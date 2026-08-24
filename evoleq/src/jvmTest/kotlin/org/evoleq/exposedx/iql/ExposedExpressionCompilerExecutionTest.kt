package org.evoleq.exposedx.iql

import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.junit.Test
import kotlin.test.assertEquals
import org.evoleq.iql.data.Expression as IqlExpression

@Suppress("LargeClass")
class ExposedExpressionCompilerExecutionTest {

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

    private val tables =
        arrayOf(
            UsersTable,
            OrdersTable,
            UserOrdersTable
        )

    private fun registry(): Registry =
        registry {

            fieldType(
                AutoIncColumnType::class,
                FieldType.LONG
            )

            entity("user", UsersTable) {

                field(UsersTable.id)
                field(UsersTable.name)

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

    private fun seed() {

        val alice =
            UsersTable.insertAndGetId {
                it[name] = "Alice"
            }

        val bob =
            UsersTable.insertAndGetId {
                it[name] = "Bob"
            }

        val charlie =
            UsersTable.insertAndGetId {
                it[name] = "Charlie"
            }

        val diana =
            UsersTable.insertAndGetId {
                it[name] = "Diana"
            }

        val aliceOrder1 =
            OrdersTable.insertAndGetId {
                it[userId] = alice
                it[amount] = 150.0
            }

        val aliceOrder2 =
            OrdersTable.insertAndGetId {
                it[userId] = alice
                it[amount] = 50.0
            }

        val bobOrder =
            OrdersTable.insertAndGetId {
                it[userId] = bob
                it[amount] = 25.0
            }

        val charlieOrder1 =
            OrdersTable.insertAndGetId {
                it[userId] = charlie
                it[amount] = 200.0
            }

        val charlieOrder2 =
            OrdersTable.insertAndGetId {
                it[userId] = charlie
                it[amount] = 300.0
            }

        UserOrdersTable.insert {
            it[userId] = alice
            it[orderId] = aliceOrder1
        }

        UserOrdersTable.insert {
            it[userId] = alice
            it[orderId] = aliceOrder2
        }

        UserOrdersTable.insert {
            it[userId] = bob
            it[orderId] = bobOrder
        }

        UserOrdersTable.insert {
            it[userId] = charlie
            it[orderId] = charlieOrder1
        }

        UserOrdersTable.insert {
            it[userId] = charlie
            it[orderId] = charlieOrder2
        }

        // Deliberately unused. This makes the empty-relation case explicit.
        require(diana.value > 0)
    }

    private fun countExpression(): IqlExpression =
        AggregateExpression(
            source =
                RelationExpression(
                    RelationRef(
                        entity = "user",
                        relation = "orders"
                    )
                ),
            aggregation = Aggregation.COUNT
        )

    private fun amountExpression(
        aggregation: Aggregation
    ): IqlExpression =
        AggregateExpression(
            source =
                MapExpression(
                    source =
                        RelationExpression(
                            RelationRef(
                                entity = "user",
                                relation = "orders"
                            )
                        ),
                    field = SimpleFieldRef("amount")
                ),
            aggregation = aggregation
        )

    private fun executeAggregate(
        expression: IqlExpression
    ): Map<String, Any?> {

        val compiler =
            ExposedExpressionCompiler(
                registry()
            )

        val compiled =
            compiler.compile(
                expression = expression,
                sourceTable = UsersTable,
                currentEntity = "user"
            )

        return UsersTable
            .slice(
                UsersTable.id,
                UsersTable.name,
                compiled.expression
            )
            .selectAll()
            .associate { row ->

                row[UsersTable.name] to
                        row[compiled.expression]
            }
    }

    /*
     * ------------------------------------------------------------
     * M:N aggregate execution
     * ------------------------------------------------------------
     */

    @Test
    fun `executes count for many-to-many relation`() =
        runSimpleH2Test(*tables) {

            seed()

            val result =
                executeAggregate(
                    countExpression()
                )

            assertEquals(
                2L,
                result["Alice"]
            )

            assertEquals(
                1L,
                result["Bob"]
            )

            assertEquals(
                2L,
                result["Charlie"]
            )

            assertEquals(
                0L,
                result["Diana"]
            )
        }

    @Test
    fun `executes min for many-to-many relation`() =
        runSimpleH2Test(*tables) {

            seed()

            val result =
                executeAggregate(
                    amountExpression(
                        Aggregation.MIN
                    )
                )

            assertEquals(
                50.0,
                result["Alice"]
            )

            assertEquals(
                25.0,
                result["Bob"]
            )

            assertEquals(
                200.0,
                result["Charlie"]
            )

            assertEquals(
                null,
                result["Diana"]
            )
        }

    @Test
    fun `executes max for many-to-many relation`() =
        runSimpleH2Test(*tables) {

            seed()

            val result =
                executeAggregate(
                    amountExpression(
                        Aggregation.MAX
                    )
                )

            assertEquals(
                150.0,
                result["Alice"]
            )

            assertEquals(
                25.0,
                result["Bob"]
            )

            assertEquals(
                300.0,
                result["Charlie"]
            )

            assertEquals(
                null,
                result["Diana"]
            )
        }

    @Test
    fun `executes sum for many-to-many relation`() =
        runSimpleH2Test(*tables) {

            seed()

            val result =
                executeAggregate(
                    amountExpression(
                        Aggregation.SUM
                    )
                )

            assertEquals(
                200.0,
                result["Alice"]
            )

            assertEquals(
                25.0,
                result["Bob"]
            )

            assertEquals(
                500.0,
                result["Charlie"]
            )

            assertEquals(
                null,
                result["Diana"]
            )
        }

    @Test
    fun `executes avg for many-to-many relation`() =
        runSimpleH2Test(*tables) {

            seed()

            val result =
                executeAggregate(
                    amountExpression(
                        Aggregation.AVG
                    )
                )

            assertEquals(
                100.0,
                result["Alice"]
            )

            assertEquals(
                25.0,
                result["Bob"]
            )

            assertEquals(
                250.0,
                result["Charlie"]
            )

            assertEquals(
                null,
                result["Diana"]
            )
        }

    /*
     * ------------------------------------------------------------
     * Expression comparison execution
     * ------------------------------------------------------------
     */

    private fun expressionComparison(
        expression: IqlExpression,
        operator: Operator,
        value: JsonPrimitive
    ): Op<Boolean> {

        val compiler =
            ExposedCompiler(
                registry()
            )

        return compiler.compile(
            filter =
                ExpressionComparisonFilter(
                    expression = expression,
                    operator = operator,
                    value = value
                ),
            table = UsersTable
        )
    }

    @Test
    fun `executes count greater than comparison`() =
        runSimpleH2Test(*tables) {

            seed()

            val predicate =
                expressionComparison(
                    expression = countExpression(),
                    operator = Operator.GT,
                    value = JsonPrimitive(1)
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Alice", "Charlie"),
                names
            )
        }

    @Test
    fun `executes count equal comparison including empty relation`() =
        runSimpleH2Test(*tables) {

            seed()

            val predicate =
                expressionComparison(
                    expression = countExpression(),
                    operator = Operator.EQ,
                    value = JsonPrimitive(0)
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Diana"),
                names
            )
        }

    @Test
    fun `executes avg greater than comparison`() =
        runSimpleH2Test(*tables) {

            seed()

            val predicate =
                expressionComparison(
                    expression =
                        amountExpression(
                            Aggregation.AVG
                        ),
                    operator = Operator.GT,
                    value = JsonPrimitive(100.0)
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie"),
                names
            )
        }

    @Test
    fun `executes max greater or equal comparison`() =
        runSimpleH2Test(*tables) {

            seed()

            val predicate =
                expressionComparison(
                    expression =
                        amountExpression(
                            Aggregation.MAX
                        ),
                    operator = Operator.GTE,
                    value = JsonPrimitive(300.0)
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie"),
                names
            )
        }

    @Test
    fun `executes sum equal comparison`() =
        runSimpleH2Test(*tables) {

            seed()

            val predicate =
                expressionComparison(
                    expression =
                        amountExpression(
                            Aggregation.SUM
                        ),
                    operator = Operator.EQ,
                    value = JsonPrimitive(500.0)
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie"),
                names
            )
        }

    @Test
    fun `executes min less than comparison`() =
        runSimpleH2Test(*tables) {

            seed()

            val predicate =
                expressionComparison(
                    expression =
                        amountExpression(
                            Aggregation.MIN
                        ),
                    operator = Operator.LT,
                    value = JsonPrimitive(100.0)
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Alice", "Bob"),
                names
            )
        }

    /*
     * ------------------------------------------------------------
     * Compound expression filters
     * ------------------------------------------------------------
     */

    @Test
    fun `combines expression comparisons with and`() =
        runSimpleH2Test(*tables) {

            seed()

            val compiler =
                ExposedCompiler(
                    registry()
                )

            val filter =
                AndFilter(
                    filters =
                        listOf(
                            ExpressionComparisonFilter(
                                expression = countExpression(),
                                operator = Operator.GT,
                                value = JsonPrimitive(1)
                            ),
                            ExpressionComparisonFilter(
                                expression =
                                    amountExpression(
                                        Aggregation.AVG
                                    ),
                                operator = Operator.GT,
                                value = JsonPrimitive(100.0)
                            )
                        )
                )

            val predicate =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie"),
                names
            )
        }

    @Test
    fun `combines expression comparisons with or`() =
        runSimpleH2Test(*tables) {

            seed()

            val compiler =
                ExposedCompiler(
                    registry()
                )

            val filter =
                OrFilter(
                    filters =
                        listOf(
                            ExpressionComparisonFilter(
                                expression = countExpression(),
                                operator = Operator.EQ,
                                value = JsonPrimitive(0)
                            ),
                            ExpressionComparisonFilter(
                                expression =
                                    amountExpression(
                                        Aggregation.MAX
                                    ),
                                operator = Operator.GT,
                                value = JsonPrimitive(250.0)
                            )
                        )
                )

            val predicate =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie", "Diana"),
                names
            )
        }

    @Test
    fun `negates expression comparison`() =
        runSimpleH2Test(*tables) {

            seed()

            val compiler =
                ExposedCompiler(
                    registry()
                )

            val filter =
                NotFilter(
                    ExpressionComparisonFilter(
                        expression = countExpression(),
                        operator = Operator.GT,
                        value = JsonPrimitive(1)
                    )
                )

            val predicate =
                compiler.compile(
                    filter = filter,
                    table = UsersTable
                )

            val names =
                UsersTable
                    .selectAll()
                    .where { predicate }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Bob", "Diana"),
                names
            )
        }

    private fun insertUsersAndOrders() {
        val aliceId =
            UsersTable.insert {
                it[name] = "Alice"
            } get UsersTable.id

        val bobId =
            UsersTable.insert {
                it[name] = "Bob"
            } get UsersTable.id

        val charlieId =
            UsersTable.insert {
                it[name] = "Charlie"
            } get UsersTable.id

        UsersTable.insert {
            it[name] = "Diana"
        }

        val aliceOrder1 =
            OrdersTable.insert {
                it[userId] = aliceId
                it[amount] = 150.0
            } get OrdersTable.id

        val aliceOrder2 =
            OrdersTable.insert {
                it[userId] = aliceId
                it[amount] = 50.0
            } get OrdersTable.id

        val bobOrder =
            OrdersTable.insert {
                it[userId] = bobId
                it[amount] = 25.0
            } get OrdersTable.id

        val charlieOrder1 =
            OrdersTable.insert {
                it[userId] = charlieId
                it[amount] = 200.0
            } get OrdersTable.id

        val charlieOrder2 =
            OrdersTable.insert {
                it[userId] = charlieId
                it[amount] = 300.0
            } get OrdersTable.id

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
    fun `executes filtered relation count comparison`() =
        runSimpleH2Test(*tables) {

            insertUsersAndOrders()

            val registry =
                registry {
                    fieldType(
                        AutoIncColumnType::class,
                        FieldType.LONG
                    )

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
                ExposedCompiler(registry)

            val expression =
                AggregateExpression(
                    source =
                        FilterExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            filter =
                                ComparisonFilter(
                                    field =
                                        SimpleFieldRef("amount"),
                                    operator =
                                        Operator.GT,
                                    value =
                                        JsonPrimitive(100.0)
                                )
                        ),
                    aggregation =
                        Aggregation.COUNT
                )

            val filter =
                ExpressionComparisonFilter(
                    expression = expression,
                    operator = Operator.GT,
                    value = JsonPrimitive(0L)
                )

            val result =
                UsersTable
                    .select(
                        UsersTable.id,
                        UsersTable.name
                    )
                    .where {
                        compiler.compile(
                            filter = filter,
                            table = UsersTable
                        )
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Alice", "Charlie"),
                result
            )
        }

    @Test
    fun `executes filtered relation count greater than one`() =
        runSimpleH2Test(*tables) {
            insertUsersAndOrders()
            val registry =
                registry {
                    fieldType(
                        AutoIncColumnType::class,
                        FieldType.LONG
                    )

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
                ExposedCompiler(registry)


            val expression =
                AggregateExpression(
                    source =
                        FilterExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            filter =
                                ComparisonFilter(
                                    field =
                                        SimpleFieldRef("amount"),
                                    operator =
                                        Operator.GT,
                                    value =
                                        JsonPrimitive(100.0)
                                )
                        ),
                    aggregation =
                        Aggregation.COUNT
                )

            val filter =
                ExpressionComparisonFilter(
                    expression = expression,
                    operator = Operator.GT,
                    value = JsonPrimitive(1L)
                )

            val result =
                UsersTable
                    .select(
                        UsersTable.id,
                        UsersTable.name
                    )
                    .where {
                        compiler.compile(
                            filter = filter,
                            table = UsersTable
                        )
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie"),
                result
            )
        }

    private fun setupTestData() {
        val aliceId =
            UsersTable.insertAndGetId {
                it[name] = "Alice"
            }.value

        val bobId =
            UsersTable.insertAndGetId {
                it[name] = "Bob"
            }.value

        val charlieId =
            UsersTable.insertAndGetId {
                it[name] = "Charlie"
            }.value

        // val dianaId =
            UsersTable.insertAndGetId {
                it[name] = "Diana"
            }.value

        val order1 =
            OrdersTable.insertAndGetId {
                it[userId] = aliceId
                it[amount] = 150.0
            }.value

        val order2 =
            OrdersTable.insertAndGetId {
                it[userId] = aliceId
                it[amount] = 50.0
            }.value

        val order3 =
            OrdersTable.insertAndGetId {
                it[userId] = bobId
                it[amount] = 25.0
            }.value

        val order4 =
            OrdersTable.insertAndGetId {
                it[userId] = charlieId
                it[amount] = 200.0
            }.value

        val order5 =
            OrdersTable.insertAndGetId {
                it[userId] = charlieId
                it[amount] = 300.0
            }.value

        UserOrdersTable.insert {
            it[userId] = aliceId
            it[orderId] = order1
        }

        UserOrdersTable.insert {
            it[userId] = aliceId
            it[orderId] = order2
        }

        UserOrdersTable.insert {
            it[userId] = bobId
            it[orderId] = order3
        }

        UserOrdersTable.insert {
            it[userId] = charlieId
            it[orderId] = order4
        }

        UserOrdersTable.insert {
            it[userId] = charlieId
            it[orderId] = order5
        }
    }

    @Test
    fun `executes filtered many-to-many count`() =
        runSimpleH2Test(*tables) {

            setupTestData()

            val compiler =
                ExposedCompiler(registry())

            val filter =
                ExpressionComparisonFilter(
                    expression =
                        AggregateExpression(
                            source =
                                FilterExpression(
                                    source =
                                        RelationExpression(
                                            relation =
                                                RelationRef(
                                                    entity = "user",
                                                    relation = "orders"
                                                )
                                        ),
                                    filter =
                                        ComparisonFilter(
                                            field =
                                                SimpleFieldRef("amount"),
                                            operator = Operator.GT,
                                            value =
                                                JsonPrimitive(100.0)
                                        )
                                ),
                            aggregation = Aggregation.COUNT
                        ),
                    operator = Operator.GT,
                    value = JsonPrimitive(0)
                )

            val result =
                UsersTable
                    .select(
                        UsersTable.id,
                        UsersTable.name
                    )
                    .where {
                        compiler.compile(
                            filter = filter,
                            table = UsersTable
                        )
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Alice", "Charlie"),
                result
            )
        }


    @Test
    fun `executes filtered many-to-many min`() =
        runSimpleH2Test(*tables) {

            setupTestData()

            val compiler =
                ExposedCompiler(registry())

            val expression =
                AggregateExpression(
                    source =
                        FilterExpression(
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
                            filter =
                                ComparisonFilter(
                                    field =
                                        SimpleFieldRef("amount"),
                                    operator = Operator.GT,
                                    value = JsonPrimitive(100.0)
                                )
                        ),
                    aggregation = Aggregation.MIN
                )

            val filter =
                ExpressionComparisonFilter(
                    expression = expression,
                    operator = Operator.GT,
                    value =
                        JsonPrimitive(100.0)
                )

            val result =
                UsersTable
                    .select(
                        UsersTable.id,
                        UsersTable.name
                    )
                    .where {
                        compiler.compile(
                            filter = filter,
                            table = UsersTable
                        )
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Alice", "Charlie"),
                result
            )
        }


    @Test
    fun `executes filtered many-to-many max`() =
        runSimpleH2Test(*tables) {

            setupTestData()

            val compiler =
                ExposedCompiler(registry())

            val expression =
                AggregateExpression(
                    source =
                        FilterExpression(
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
                            filter =
                                ComparisonFilter(
                                    field =
                                        SimpleFieldRef("amount"),
                                    operator = Operator.GT,
                                    value =
                                        JsonPrimitive(100.0)
                                )
                        ),
                    aggregation = Aggregation.MAX
                )

            val filter =
                ExpressionComparisonFilter(
                    expression = expression,
                    operator = Operator.GT,
                    value =
                        JsonPrimitive(250.0)
                )

            val result =
                UsersTable
                    .select(
                        UsersTable.id,
                        UsersTable.name
                    )
                    .where {
                        compiler.compile(
                            filter = filter,
                            table = UsersTable
                        )
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
    fun `executes filtered many-to-many sum`() =
        runSimpleH2Test(*tables) {

            setupTestData()

            val compiler =
                ExposedCompiler(registry())

            val expression =
                AggregateExpression(
                    source =
                        FilterExpression(
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
                            filter =
                                ComparisonFilter(
                                    field =
                                        SimpleFieldRef("amount"),
                                    operator = Operator.GT,
                                    value =
                                        JsonPrimitive(100.0)
                                )
                        ),
                    aggregation = Aggregation.SUM
                )

            val filter =
                ExpressionComparisonFilter(
                    expression = expression,
                    operator = Operator.GT,
                    value =
                        JsonPrimitive(300.0)
                )

            val result =
                UsersTable
                    .select(
                        UsersTable.id,
                        UsersTable.name
                    )
                    .where {
                        compiler.compile(
                            filter = filter,
                            table = UsersTable
                        )
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
    fun `executes filtered many-to-many avg`() =
        runSimpleH2Test(*tables) {

            setupTestData()

            val compiler =
                ExposedCompiler(registry())

            val expression =
                AggregateExpression(
                    source =
                        FilterExpression(
                            source =
                                RelationExpression(
                                    relation =
                                        RelationRef(
                                            entity = "user",
                                            relation = "orders"
                                        )
                                ),
                            filter =
                                ComparisonFilter(
                                    field =
                                        SimpleFieldRef("amount"),
                                    operator = Operator.GT,
                                    value =
                                        JsonPrimitive(100.0)
                                )
                        ),
                    aggregation = Aggregation.AVG
                )

            val filter =
                ExpressionComparisonFilter(
                    expression = expression,
                    operator = Operator.GT,
                    value =
                        JsonPrimitive(200.0)
                )

            val result =
                UsersTable
                    .select(
                        UsersTable.id,
                        UsersTable.name
                    )
                    .where {
                        compiler.compile(
                            filter = filter,
                            table = UsersTable
                        )
                    }
                    .map {
                        it[UsersTable.name]
                    }

            assertEquals(
                listOf("Charlie"),
                result
            )
        }
}
