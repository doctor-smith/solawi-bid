package org.evoleq.exposedx.iql

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.date
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class JodaDateTimeFieldDefinition : FieldDefinition {

    override val type =
        FieldType.DATETIME

    override fun translate(
        value: JsonElement
    ): Any =
        DateTime.parse(
            value.jsonPrimitive.content
        )

    @Suppress("UNCHECKED_CAST")
    override fun compileComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val typedColumn =
            column as Column<DateTime>

        val typedValue =
            translate(value) as DateTime

        return when (operator) {

            Operator.EQ ->
                typedColumn eq typedValue

            Operator.NE ->
                typedColumn neq typedValue

            Operator.GT ->
                typedColumn greater typedValue

            Operator.GTE ->
                typedColumn greaterEq typedValue

            Operator.LT ->
                typedColumn less typedValue

            Operator.LTE ->
                typedColumn lessEq typedValue

            Operator.LIKE -> throw UnsupportedOperationException()
        }
    }

    override fun compileIn(
        column: Column<*>,
        values: List<JsonElement>
    ): Op<Boolean> {

        val typedColumn =
            column as Column<DateTime>

        val typedValues =
            values.map {
                translate(it) as DateTime
            }

        return typedColumn inList typedValues
    }
}





class JodaIqlTest {

    // -------------------------------------------------------------------------
    // Translation
    // -------------------------------------------------------------------------

    @Test
    fun `DATETIME translates JSON to Joda DateTime`() {

        val definition =
            JodaDateTimeFieldDefinition()

        val value =
            definition.translate(
                JsonPrimitive(
                    "2026-08-20T12:30:00.000Z"
                )
            )

        assertEquals(
            DateTime.parse(
                "2026-08-20T12:30:00.000Z"
            ),
            value
        )
    }



    // -------------------------------------------------------------------------
    // Registry
    // -------------------------------------------------------------------------

    object Events : Table("events") {

        val id =
            integer("id")

        val createdAt =
            datetime("created_at")

        val eventDate =
            date("event_date")
    }


    @Test
    fun `registry resolves Joda DATETIME definition`() {

        val registry =
            Registry().apply {
                registerFieldDefinition(
                    JodaDateTimeFieldDefinition()
                )
            }

        val definition =
            registry.lookup(
                FieldType.DATETIME
            )

        assertEquals(
            FieldType.DATETIME,
            definition.type
        )

        assertEquals(
            DateTime.parse(
                "2026-08-20T12:30:00.000Z"
            ),
            definition.translate(
                JsonPrimitive(
                    "2026-08-20T12:30:00.000Z"
                )
            )
        )
    }



    // -------------------------------------------------------------------------
    // IQL → Exposed
    // -------------------------------------------------------------------------

    @Test
    fun `datetime comparison is compiled`() = runSimpleH2Test(Events){

        val registry =
            testRegistry()

        val filter =
            ComparisonFilter(
                field = SimpleFieldRef(
                    "Event.created_at"
                ),
                operator = Operator.GT,
                value = JsonPrimitive(
                    "2026-08-20T12:00:00.000Z"
                )
            )

        val expression =
            ExposedCompiler(registry)
                .compile(
                    filter,
                    Events
                )

        assertTrue(
            expression.toString().isNotBlank()
        )
    }


    @Test
    fun `datetime IN expression is compiled`() = runSimpleH2Test(Events){

        val registry =
            testRegistry()

        val filter =
            InFilter(
                field = SimpleFieldRef(
                    "Event.created_at"
                ),
                values = listOf(
                    JsonPrimitive(
                        "2026-08-20T12:00:00.000Z"
                    ),
                    JsonPrimitive(
                        "2026-08-21T12:00:00.000Z"
                    )
                )
            )

        val expression =
            ExposedCompiler(registry)
                .compile(
                    filter,
                    Events
                )

        assertTrue(
            expression.toString().isNotBlank()
        )
    }


    // -------------------------------------------------------------------------
    // Executable documentation
    // -------------------------------------------------------------------------

    @Test
    fun `IQL datetime remains backend independent`() = runSimpleH2Test(Events){

        val filter =
            ComparisonFilter(
                field = SimpleFieldRef(
                    "Event.created_at"
                ),
                operator = Operator.GTE,
                value = JsonPrimitive(
                    "2026-08-20T12:00:00.000Z"
                )
            )

        /*
         * The IQL layer knows only:
         *
         *     DATETIME
         *     +
         *     JSON value
         *
         * It does not know Joda DateTime.
         *
         * The backend Registry translates the value.
         */

        val registry =
            testRegistry()

        val definition =
            registry.lookup(
                FieldType.DATETIME
            )

        val backendValue =
            definition.translate(
                filter.value
            )

        assertTrue(
            backendValue is DateTime
        )
    }

    // -------------------------------------------------------------------------
    // Test infrastructure
    // -------------------------------------------------------------------------

    private fun testRegistry(): Registry =
        Registry().apply {

            registerEntity(
                entity = EntityType(
                    name = "Event",
                    table = "events",
                    fields = mapOf(
                        "id" to FieldInfo(
                            name = "id",
                            type = FieldType.INTEGER
                        ),
                        "created_at" to FieldInfo(
                            name = "created_at",
                            type = FieldType.DATETIME
                        ),
                        "event_date" to FieldInfo(
                            name = "event_date",
                            type = FieldType.DATE
                        )
                    ),
                    relations = emptyMap()
                ),
                table = Events
            )

            registerFieldDefinition(
                JodaDateTimeFieldDefinition()
            )
        }
}
