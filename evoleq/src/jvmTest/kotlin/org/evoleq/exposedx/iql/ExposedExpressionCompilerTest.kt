package org.evoleq.exposedx.iql

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("LargeClass")
class ExposedExpressionCompilerTest {

    object UsersTable : LongIdTable("users") {
        val name = varchar("name", 255)
    }

    object OrdersTable : LongIdTable("orders") {
        val userId = reference("user_id", UsersTable)
        val amount = double("amount")
    }

    object UserOrdersTable : Table("user_orders") {
        val userId = reference("user_id", UsersTable)
        val orderId = reference("order_id", OrdersTable)

        override val primaryKey =
            PrimaryKey(userId, orderId)
    }

    val tables = arrayOf(
        UsersTable,
        OrdersTable,
        UserOrdersTable
    )

    /*
     * ------------------------------------------------------------
     * COUNT
     * ------------------------------------------------------------
     */

    @Test
    fun `compiles relation count as LONG expression`() =
        runSimpleH2Test(*tables) {

            val registry =
                registry {
                    fieldType(AutoIncColumnType::class, FieldType.LONG)

                    entity("user", UsersTable) {

                        manyToMany(
                            "orders",
                            OrdersTable,
                            UserOrdersTable
                        ) {
                            source(
                                UsersTable.id references UserOrdersTable.userId
                            )

                            target(
                                OrdersTable.id references UserOrdersTable.orderId
                            )
                        }
                    }

                    entity("order", OrdersTable) {
                        field(OrdersTable.id)
                        field(OrdersTable.userId)
                        field(OrdersTable.amount)
                    }
                }

            val compiler =
                ExposedExpressionCompiler(registry)

            val expression =
                AggregateExpression(
                    source =
                        RelationExpression(
                            relation =
                                RelationRef(
                                    entity = "user",
                                    relation = "orders"
                                )
                        ),
                    aggregation = Aggregation.COUNT
                )

            val compiled =
                compiler.compile(
                    expression = expression,
                    sourceTable = UsersTable,
                    currentEntity = "user"
                )

            assertEquals(
                FieldType.LONG,
                compiled.fieldType
            )

            assertTrue(
                compiled.expression is ExpressionWithColumnType<*>
            )
        }


    /*
     * ------------------------------------------------------------
     * MIN / MAX
     * ------------------------------------------------------------
     */

    @Test
    fun `compiles mapped relation min preserving field type`() =
        runSimpleH2Test(*tables) {

            val registry =
                registry {
                    fieldType(AutoIncColumnType::class, FieldType.LONG)

                    entity("user", UsersTable) {

                        manyToMany(
                            "orders",
                            OrdersTable,
                            UserOrdersTable
                        ) {
                            source(
                                UsersTable.id references UserOrdersTable.userId
                            )

                            target(
                                OrdersTable.id references UserOrdersTable.orderId
                            )
                        }
                    }

                    entity("order", OrdersTable) {
                        field(OrdersTable.id)
                        field(OrdersTable.userId)
                        field(OrdersTable.amount)
                    }
                }

            val compiler =
                ExposedExpressionCompiler(registry)

            val expression =
                AggregateExpression(
                    source =
                        MapExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            field =
                                SimpleFieldRef("amount")
                        ),
                    aggregation = Aggregation.MIN
                )

            val compiled =
                compiler.compile(
                    expression = expression,
                    sourceTable = UsersTable,
                    currentEntity = "user"
                )

            assertEquals(
                FieldType.DOUBLE,
                compiled.fieldType
            )

            assertTrue(
                compiled.expression is ExpressionWithColumnType<*>
            )
        }


    @Test
    fun `compiles mapped relation max preserving field type`() =
        runSimpleH2Test(*tables) {

            val registry =
                registry {
                    fieldType(AutoIncColumnType::class, FieldType.LONG)

                    entity("user", UsersTable) {

                        manyToMany(
                            "orders",
                            OrdersTable,
                            UserOrdersTable
                        ) {
                            source(
                                UsersTable.id references UserOrdersTable.userId
                            )

                            target(
                                OrdersTable.id references UserOrdersTable.orderId
                            )
                        }
                    }

                    entity("order", OrdersTable) {
                        field(OrdersTable.id)
                        field(OrdersTable.userId)
                        field(OrdersTable.amount)
                    }
                }

            val compiler =
                ExposedExpressionCompiler(registry)

            val expression =
                AggregateExpression(
                    source =
                        MapExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            field =
                                SimpleFieldRef("amount")
                        ),
                    aggregation = Aggregation.MAX
                )

            val compiled =
                compiler.compile(
                    expression = expression,
                    sourceTable = UsersTable,
                    currentEntity = "user"
                )

            assertEquals(
                FieldType.DOUBLE,
                compiled.fieldType
            )
        }


    /*
     * ------------------------------------------------------------
     * AVG
     * ------------------------------------------------------------
     */

    @Test
    fun `compiles avg as DOUBLE`() =
        runSimpleH2Test(*tables) {

            val registry =
                registry {

                    fieldType(AutoIncColumnType::class, FieldType.LONG)

                    entity("user", UsersTable) {

                        manyToMany(
                            "orders",
                            OrdersTable,
                            UserOrdersTable
                        ) {
                            source(
                                UsersTable.id references UserOrdersTable.userId
                            )

                            target(
                                OrdersTable.id references UserOrdersTable.orderId
                            )
                        }
                    }

                    entity("order", OrdersTable) {
                        field(OrdersTable.id)
                        field(OrdersTable.userId)
                        field(OrdersTable.amount)
                    }
                }

            val compiler =
                ExposedExpressionCompiler(registry)

            val expression =
                AggregateExpression(
                    source =
                        MapExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            field =
                                SimpleFieldRef("amount")
                        ),
                    aggregation = Aggregation.AVG
                )

            val compiled =
                compiler.compile(
                    expression = expression,
                    sourceTable = UsersTable,
                    currentEntity = "user"
                )

            assertEquals(
                FieldType.DOUBLE,
                compiled.fieldType
            )

            assertTrue(
                compiled.expression is ExpressionWithColumnType<*>
            )
        }


    /*
     * ------------------------------------------------------------
     * ExpressionComparisonFilter
     * ------------------------------------------------------------
     */

    @Test
    fun `compiles count expression comparison`() =
        runSimpleH2Test(*tables) {

            val registry =
                registry {
                    fieldType(AutoIncColumnType::class, FieldType.LONG)

                    entity("user", UsersTable) {

                        manyToMany(
                            "orders",
                            OrdersTable,
                            UserOrdersTable
                        ) {
                            source(
                                UsersTable.id references UserOrdersTable.userId
                            )

                            target(
                                OrdersTable.id references UserOrdersTable.orderId
                            )
                        }
                    }

                    entity("order", OrdersTable) {
                        field(OrdersTable.id)
                        field(OrdersTable.userId)
                        field(OrdersTable.amount)
                    }
                }

            val compiler =
                ExposedCompiler(
                    registry
                )

            val filter =
                ExpressionComparisonFilter(
                    expression =
                        AggregateExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            aggregation = Aggregation.COUNT
                        ),
                    operator = Operator.GT,
                    value = kotlinx.serialization.json.JsonPrimitive(1)
                )

            val result =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            assertTrue(
                result is Op<*>
            )
        }


    @Test
    fun `compiles avg expression comparison`() =
        runSimpleH2Test(*tables) {

            val registry =
                registry {
                    fieldType(AutoIncColumnType::class, FieldType.LONG)

                    entity("user", UsersTable) {

                        manyToMany(
                            "orders",
                            OrdersTable,
                            UserOrdersTable
                        ) {
                            source(
                                UsersTable.id references UserOrdersTable.userId
                            )

                            target(
                                OrdersTable.id references UserOrdersTable.orderId
                            )
                        }
                    }

                    entity("order", OrdersTable) {
                        field(OrdersTable.id)
                        field(OrdersTable.userId)
                        field(OrdersTable.amount)
                    }
                }

            val compiler =
                ExposedCompiler(
                    registry
                )

            val filter =
                ExpressionComparisonFilter(
                    expression =
                        AggregateExpression(
                            source =
                                MapExpression(
                                    source =
                                        RelationExpression(
                                            relation =
                                                RelationRef(
                                                    entity = "user",
                                                    relation = "orders"
                                                )
                                        ),
                                    field =
                                        SimpleFieldRef("amount")
                                ),
                            aggregation = Aggregation.AVG
                        ),
                    operator = Operator.GT,
                    value =
                        kotlinx.serialization.json.JsonPrimitive(100.0)
                )

            val result =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            assertTrue(
                result is Op<*>
            )
        }


    // test setup for next tests
    private fun testRegistry() =
        registry {
            fieldType(AutoIncColumnType::class, FieldType.LONG)

            entity("user", UsersTable) {

                manyToMany(
                    "orders",
                    OrdersTable,
                    UserOrdersTable
                ) {
                    source(
                        UsersTable.id references UserOrdersTable.userId
                    )

                    target(
                        OrdersTable.id references UserOrdersTable.orderId
                    )
                }
            }

            entity("order", OrdersTable) {
                field(OrdersTable.id)
                field(OrdersTable.userId)
                field(OrdersTable.amount)
            }
        }

    private fun insertTestData() {

        val aliceId =
            UsersTable.insertAndGetId {
                it[name] = "Alice"
            }

        val bobId =
            UsersTable.insertAndGetId {
                it[name] = "Bob"
            }

        val charlieId =
            UsersTable.insertAndGetId {
                it[name] = "Charlie"
            }

        val aliceOrder1 =
            OrdersTable.insertAndGetId {
                it[userId] = aliceId
                it[amount] = 150.0
            }

        val aliceOrder2 =
            OrdersTable.insertAndGetId {
                it[userId] = aliceId
                it[amount] = 50.0
            }

        val bobOrder =
            OrdersTable.insertAndGetId {
                it[userId] = bobId
                it[amount] = 25.0
            }

        val charlieOrder1 =
            OrdersTable.insertAndGetId {
                it[userId] = charlieId
                it[amount] = 200.0
            }

        val charlieOrder2 =
            OrdersTable.insertAndGetId {
                it[userId] = charlieId
                it[amount] = 300.0
            }

        UserOrdersTable.insert {
            it[userId] = aliceId
            it[orderId] = aliceOrder1
        }

        UserOrdersTable.insert {
            it[userId] = aliceId
            it[orderId] = aliceOrder2
        }

        UserOrdersTable.insert {
            it[userId] = bobId
            it[orderId] = bobOrder
        }

        UserOrdersTable.insert {
            it[userId] = charlieId
            it[orderId] = charlieOrder1
        }

        UserOrdersTable.insert {
            it[userId] = charlieId
            it[orderId] = charlieOrder2
        }
    }

    @Test
    fun `executes count expression comparison`() =
        runSimpleH2Test(*tables) {

            insertTestData()

            val registry =
                testRegistry()

            val compiler =
                ExposedCompiler(registry)

            val filter =
                ExpressionComparisonFilter(
                    expression =
                        AggregateExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            aggregation = Aggregation.COUNT
                        ),
                    operator = Operator.GT,
                    value =
                        kotlinx.serialization.json.JsonPrimitive(1)
                )

            val predicate =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            val result =
                UsersTable
                    .selectAll()
                    .where {
                        predicate
                    }
                    .orderBy(UsersTable.name)
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Alice", "Charlie"),
                result
            )
        }

    @Test
    fun `executes avg expression comparison`() =
        runSimpleH2Test(*tables) {

            insertTestData()

            val registry =
                testRegistry()

            val compiler =
                ExposedCompiler(registry)

            val filter =
                ExpressionComparisonFilter(
                    expression =
                        AggregateExpression(
                            source =
                                MapExpression(
                                    source =
                                        RelationExpression(
                                            relation =
                                                RelationRef(
                                                    entity = "user",
                                                    relation = "orders"
                                                )
                                        ),
                                    field =
                                        SimpleFieldRef("amount")
                                ),
                            aggregation = Aggregation.AVG
                        ),
                    operator = Operator.GT,
                    value =
                        kotlinx.serialization.json.JsonPrimitive(100.0)
                )

            val predicate =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            val result =
                UsersTable
                    .selectAll()
                    .where {
                        predicate
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie"),
                result
            )
        }

    @Test
    fun `executes min expression comparison`() =
        runSimpleH2Test(*tables) {

            insertTestData()

            val registry =
                testRegistry()

            val compiler =
                ExposedCompiler(registry)

            val filter =
                ExpressionComparisonFilter(
                    expression =
                        AggregateExpression(
                            source =
                                MapExpression(
                                    source =
                                        RelationExpression(
                                            relation =
                                                RelationRef(
                                                    entity = "user",
                                                    relation = "orders"
                                                )
                                        ),
                                    field =
                                        SimpleFieldRef("amount")
                                ),
                            aggregation = Aggregation.MIN
                        ),
                    operator = Operator.LT,
                    value =
                        kotlinx.serialization.json.JsonPrimitive(100.0)
                )

            val predicate =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            val result =
                UsersTable
                    .selectAll()
                    .where {
                        predicate
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Alice", "Bob"),
                result
            )
        }

    @Test
    fun `executes max expression comparison`() =
        runSimpleH2Test(*tables) {

            insertTestData()

            val registry =
                testRegistry()

            val compiler =
                ExposedCompiler(registry)

            val filter =
                ExpressionComparisonFilter(
                    expression =
                        AggregateExpression(
                            source =
                                MapExpression(
                                    source =
                                        RelationExpression(
                                            relation =
                                                RelationRef(
                                                    entity = "user",
                                                    relation =
                                                        "orders"
                                                )
                                        ),
                                    field =
                                        SimpleFieldRef("amount")
                                ),
                            aggregation = Aggregation.MAX
                        ),
                    operator = Operator.GTE,
                    value =
                        kotlinx.serialization.json.JsonPrimitive(300.0)
                )

            val predicate =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            val result =
                UsersTable
                    .selectAll()
                    .where {
                        predicate
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie"),
                result
            )
        }

    @Test
    fun `count expression returns zero for user without orders`() =
        runSimpleH2Test(*tables) {

            val aliceId =
                UsersTable.insertAndGetId {
                    it[name] = "Alice"
                }

            UsersTable.insertAndGetId {
                it[name] = "Bob"
            }

            val orderId =
                OrdersTable.insertAndGetId {
                    it[userId] = aliceId
                    it[amount] = 50.0
                }

            UserOrdersTable.insert {
                it[userId] = aliceId
                it[UserOrdersTable.orderId] = orderId
            }

            val registry =
                testRegistry()

            val compiler =
                ExposedCompiler(registry)

            val filter =
                ExpressionComparisonFilter(
                    expression =
                        AggregateExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            aggregation = Aggregation.COUNT
                        ),
                    operator = Operator.EQ,
                    value =
                        kotlinx.serialization.json.JsonPrimitive(0)
                )

            val predicate =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            val result =
                UsersTable
                    .selectAll()
                    .where {
                        predicate
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Bob"),
                result
            )
        }
}
