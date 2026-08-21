package org.evoleq.exposedx.iql

import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals

object Users : Table("users") {
    val id = integer("id")
    val name = varchar("name", 255)
    val age = integer("age")
    val active = bool("active")
}

object Orders : Table("orders") {
    val id = integer("id")
    val userId = integer("user_id")
    val amount = double("amount")
}

class ExposedCompilerTest {

    val tables = arrayOf(Users, Orders)

    private val registry = Registry().apply {

        registerEntity(
            EntityType(
                name = "user",
                table = "users",
                fields = mapOf(
                    "id" to FieldInfo("id", FieldType.INTEGER),
                    "name" to FieldInfo("name", FieldType.STRING),
                    "age" to FieldInfo("age", FieldType.INTEGER),
                    "active" to FieldInfo("active", FieldType.BOOLEAN)
                ),
                relations = mapOf(
                    "orders" to RelationInfo(
                        name = "orders",
                        type = RelationType.ONE_TO_MANY,
                        targetEntity = "order",
                        joinColumns = listOf("id"),
                        inverseJoinColumn = "user_id"
                    )
                )
            ),
            Users
        )

        registerEntity(
            EntityType(
                name = "order",
                table = "orders",
                fields = mapOf(
                    "id" to FieldInfo("id", FieldType.INTEGER),
                    "user_id" to FieldInfo("user_id", FieldType.INTEGER),
                    "amount" to FieldInfo("amount", FieldType.DOUBLE)
                ),
                relations = emptyMap()
            ),
            Orders
        )
    }

    private val compiler = ExposedCompiler(registry)


    fun setup() {
        transaction {
// Alice: adult, active, orders 150 + 50
            Users.insert {
                it[id] = 1
                it[name] = "Alice"
                it[age] = 25
                it[active] = true
            }

            // Bob: minor, active, order 25
            Users.insert {
                it[id] = 2
                it[name] = "Bob"
                it[age] = 17
                it[active] = true
            }

            // Charlie: adult, inactive, orders 200 + 300
            Users.insert {
                it[id] = 3
                it[name] = "Charlie"
                it[age] = 42
                it[active] = false
            }

            // Diana: adult, active, no orders
            Users.insert {
                it[id] = 4
                it[name] = "Diana"
                it[age] = 30
                it[active] = true
            }

            Orders.insert {
                it[id] = 100
                it[userId] = 1
                it[amount] = 150.0
            }

            Orders.insert {
                it[id] = 101
                it[userId] = 1
                it[amount] = 50.0
            }

            Orders.insert {
                it[id] = 102
                it[userId] = 2
                it[amount] = 25.0
            }

            Orders.insert {
                it[id] = 103
                it[userId] = 3
                it[amount] = 200.0
            }

            Orders.insert {
                it[id] = 104
                it[userId] = 3
                it[amount] = 300.0
            }
        }
    }

    private fun query(filter: Filter): List<String> =
        transaction {
            Users
                .selectAll()
                .where {
                    compiler.compile(filter, Users)
                }
                .map {
                    it[Users.name]
                }
        }

    // -------------------------------------------------------------------------
    // Simple comparisons
    // -------------------------------------------------------------------------

    @Test
    fun `age greater than or equal to 18`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            ComparisonFilter(
                field = SimpleFieldRef("user.age"),
                operator = Operator.GTE,
                value = JsonPrimitive(18)
            )

        assertEquals(
            listOf("Alice", "Charlie", "Diana"),
            query(filter)
        )
    }

    @Test
    fun `active equals true`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            ComparisonFilter(
                field = SimpleFieldRef("user.active"),
                operator = Operator.EQ,
                value = JsonPrimitive(true)
            )

        assertEquals(
            listOf("Alice", "Bob", "Diana"),
            query(filter)
        )
    }

    // -------------------------------------------------------------------------
    // Logical operators
    // -------------------------------------------------------------------------

    @Test
    fun `adult and active`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            AndFilter(
                listOf(
                    ComparisonFilter(
                        field = SimpleFieldRef("user.age"),
                        operator = Operator.GTE,
                        value = JsonPrimitive(18)
                    ),
                    ComparisonFilter(
                        field = SimpleFieldRef("user.active"),
                        operator = Operator.EQ,
                        value = JsonPrimitive(true)
                    )
                )
            )

        assertEquals(
            listOf("Alice", "Diana"),
            query(filter)
        )
    }

    @Test
    fun `minor or inactive`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            OrFilter(
                listOf(
                    ComparisonFilter(
                        field = SimpleFieldRef("user.age"),
                        operator = Operator.LT,
                        value = JsonPrimitive(18)
                    ),
                    ComparisonFilter(
                        field = SimpleFieldRef("user.active"),
                        operator = Operator.EQ,
                        value = JsonPrimitive(false)
                    )
                )
            )

        assertEquals(
            listOf("Bob", "Charlie"),
            query(filter)
        )
    }

    @Test
    fun `not active`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            NotFilter(
                ComparisonFilter(
                    field = SimpleFieldRef("user.active"),
                    operator = Operator.EQ,
                    value = JsonPrimitive(true)
                )
            )

        assertEquals(
            listOf("Charlie"),
            query(filter)
        )
    }

    // -------------------------------------------------------------------------
    // IN
    // -------------------------------------------------------------------------

    @Test
    fun `age in 17 25 42`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            InFilter(
                field = SimpleFieldRef("user.age"),
                values = listOf(
                    JsonPrimitive(17),
                    JsonPrimitive(25),
                    JsonPrimitive(42)
                )
            )

        assertEquals(
            listOf("Alice", "Bob", "Charlie"),
            query(filter)
        )
    }

    // -------------------------------------------------------------------------
    // ANY
    // -------------------------------------------------------------------------

    @Test
    fun `ANY order greater than 100`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            QuantifierFilter(
                quantifier = Quantifier.ANY,
                relation = RelationRef(
                    entity = "user",
                    relation = "orders"
                ),
                filter = ComparisonFilter(
                    field = SimpleFieldRef("order.amount"),
                    operator = Operator.GT,
                    value = JsonPrimitive(100.0)
                )
            )

        assertEquals(
            listOf("Alice", "Charlie"),
            query(filter)
        )
    }

    // -------------------------------------------------------------------------
    // NONE
    // -------------------------------------------------------------------------

    @Test
    fun `NONE order greater than 100`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            QuantifierFilter(
                quantifier = Quantifier.NONE,
                relation = RelationRef(
                    entity = "user",
                    relation = "orders"
                ),
                filter = ComparisonFilter(
                    field = SimpleFieldRef("order.amount"),
                    operator = Operator.GT,
                    value = JsonPrimitive(100.0)
                )
            )

        assertEquals(
            listOf("Bob", "Diana"),
            query(filter)
        )
    }

    // -------------------------------------------------------------------------
    // ALL
    // -------------------------------------------------------------------------

    @Test
    fun `ALL orders greater than 100`() = runSimpleH2Test(*tables){
        setup()

        val filter =
            QuantifierFilter(
                quantifier = Quantifier.ALL,
                relation = RelationRef(
                    entity = "user",
                    relation = "orders"
                ),
                filter = ComparisonFilter(
                    field = SimpleFieldRef("order.amount"),
                    operator = Operator.GT,
                    value = JsonPrimitive(100.0)
                )
            )

        /*
         * Logical ALL:
         *
         * NOT EXISTS (
         *   order
         *   WHERE user_id = users.id
         *   AND NOT amount > 100
         * )
         *
         * Therefore:
         *
         * Alice   -> false (50)
         * Bob     -> false (25)
         * Charlie -> true  (200, 300)
         * Diana   -> true  (no counterexample exists)
         */

        assertEquals(
            listOf("Charlie", "Diana"),
            query(filter)
        )
    }

    // -------------------------------------------------------------------------
    // ALL + existence
    // -------------------------------------------------------------------------

    @Test
    fun `ALL orders greater than 100 and at least one order exists`() = runSimpleH2Test(*tables){
        setup()

        val all =
            QuantifierFilter(
                quantifier = Quantifier.ALL,
                relation = RelationRef(
                    entity = "user",
                    relation = "orders"
                ),
                filter = ComparisonFilter(
                    field = SimpleFieldRef("order.amount"),
                    operator = Operator.GT,
                    value = JsonPrimitive(100.0)
                )
            )

        val any =
            QuantifierFilter(
                quantifier = Quantifier.ANY,
                relation = RelationRef(
                    entity = "user",
                    relation = "orders"
                ),
                filter = ComparisonFilter(
                    field = SimpleFieldRef("order.amount"),
                    operator = Operator.GT,
                    value = JsonPrimitive(0.0)
                )
            )

        val filter =
            AndFilter(
                listOf(all, any)
            )

        assertEquals(
            listOf("Charlie"),
            query(filter)
        )
    }




    @Test
    fun `LIKE contains finds matching users`() =
        runSimpleH2Test(
            Users
        ) {
            Users.insert {
                it[id] = 1
                it[name] = "Florian"
                it[age] = 10
                it[active] = true
            }

            Users.insert {
                it[id] = 2
                it[name] = "Alice"
                it[age] = 10
                it[active] = true
            }

            val filter =
                ComparisonFilter(
                    field = SimpleFieldRef("user.name"),
                    operator = Operator.LIKE,
                    value = JsonPrimitive("%lor%")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        compiler.compile(filter, Users)
                    }
                    .map {
                        it[Users.id]
                    }

            assertEquals(
                listOf(1),
                result
            )
        }

    @Test
    fun `LIKE startsWith finds matching users`() =
        runSimpleH2Test(
            Users
        ) {
            Users.insert {
                it[id] = 1
                it[name] = "Florian"
                it[age] = 10
                it[active] = true
            }

            Users.insert {
                it[id] = 2
                it[name] = "Alice"
                it[age] = 10
                it[active] = true
            }

            val filter =
                ComparisonFilter(
                    field = SimpleFieldRef("user.name"),
                    operator = Operator.LIKE,
                    value = JsonPrimitive("Flo%")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        compiler.compile(filter, Users)
                    }
                    .map {
                        it[Users.id]
                    }

            assertEquals(
                listOf(1),
                result
            )
        }

    @Test
    fun `LIKE endsWith finds matching users`() =
        runSimpleH2Test(
            Users
        ) {
            Users.insert {
                it[id] = 1
                it[name] = "Florian"
                it[age] = 10
                it[active] = true
            }

            Users.insert {
                it[id] = 2
                it[name] = "Alice"
                it[age] = 10
                it[active] = true
            }

            val filter =
                ComparisonFilter(
                    field = SimpleFieldRef("user.name"),
                    operator = Operator.LIKE,
                    value = JsonPrimitive("%ian")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        compiler.compile(filter, Users)
                    }
                    .map {
                        it[Users.id]
                    }

            assertEquals(
                listOf(1),
                result
            )
        }

    @Test
    fun `compile simple comparison`() = runSimpleH2Test(
        Users,
        Orders
    ){

        val filter = ComparisonFilter(
            field = SimpleFieldRef("user.age"),
            operator = Operator.GTE,
            value = JsonPrimitive(18)
        )

        val expression =
            compiler.compile(
                filter = filter,
                table = Users
            )

        println(expression)
    }

    @Test
    fun `contains treats underscore as literal`() =
        runSimpleH2Test(Users) {

            Users.insert {
                it[id] = 1
                it[name] = "100_kg"
                it[age] = 18
                it[active] = true
            }

            Users.insert {
                it[id] = 2
                it[name] = "100Xkg"
                it[age] = 18
                it[active] = true
            }

            val filter =
                ComparisonFilter(
                    field = SimpleFieldRef("user.name"),
                    operator = Operator.LIKE,
                    value = JsonPrimitive("%100\\_kg%")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        compiler.compile(filter, Users)
                    }
                    .map { it[Users.id] }

            assertEquals(
                listOf(1),
                result
            )
        }
    @Test
    fun `startsWith treats underscore as literal`() =
        runSimpleH2Test(Users) {

            Users.insert {
                it[id] = 1
                it[name] = "100_kg"
                it[age] = 100
                it[active] = true
            }

            Users.insert {
                it[id] = 2
                it[name] = "100Xkg"
                it[age] = 100
                it[active] = true
            }

            val filter =
                ComparisonFilter(
                    field = SimpleFieldRef("user.name"),
                    operator = Operator.LIKE,
                    value = JsonPrimitive("100\\_kg%")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        compiler.compile(filter, Users)
                    }
                    .map { it[Users.id] }

            assertEquals(
                listOf(1),
                result
            )
        }

    @Test
    fun `endsWith treats underscore as literal`() =
        runSimpleH2Test(Users) {

            Users.insert {
                it[id] = 1
                it[name] = "foo_kg"
                it[age] = 100
                it[active] = true
            }

            Users.insert {
                it[id] = 2
                it[name] = "fooXkg"
                it[age] = 100
                it[active] = true
            }

            val filter =
                ComparisonFilter(
                    field = SimpleFieldRef("user.name"),
                    operator = Operator.LIKE,
                    value = JsonPrimitive("%foo\\_kg")
                )

            val result =
                Users
                    .selectAll()
                    .where {
                        compiler.compile(filter, Users)
                    }
                    .map { it[Users.id] }

            assertEquals(
                listOf(1),
                result
            )
        }
}
