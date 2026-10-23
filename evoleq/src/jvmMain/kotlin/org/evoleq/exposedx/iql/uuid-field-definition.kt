package org.evoleq.exposedx.iql

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.evoleq.iql.data.FieldType
import org.evoleq.iql.data.Operator
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import java.util.*

class UUIDFieldDefinition : FieldDefinition {

    override val type =
        FieldType.UUID

    override fun translate(
        value: JsonElement
    ): Any =
        UUID.fromString(
            value.jsonPrimitive.content
        )

    @Suppress("UNCHECKED_CAST")
    override fun compileComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val typedColumn =
            column as Column<UUID>

        val typedValue =
            translate(value) as UUID

        return when (operator) {

            Operator.EQ ->
                typedColumn eq typedValue

            Operator.NE ->
                typedColumn neq typedValue

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE,
            Operator.LIKE -> throw UnsupportedOperationException()
        }
    }

    override fun compileIn(
        column: Column<*>,
        values: List<JsonElement>
    ): Op<Boolean> {

        val typedColumn =
            column as Column<UUID>

        val typedValues =
            values.map {
                translate(it) as UUID
            }

        return typedColumn inList typedValues
    }
}
