package org.evoleq.exposedx.iql

import kotlinx.serialization.json.JsonElement
import org.evoleq.iql.data.FieldType
import org.evoleq.iql.data.Operator
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq

enum class FieldNameStrategy {
    EXACT,
    SNAKE_CASE
}


interface FieldDefinition {

    val type: org.evoleq.iql.data.FieldType

    fun translate(value: JsonElement): Any

    fun compileComparison(
        column: Column<*>,
        operator: org.evoleq.iql.data.Operator,
        value: JsonElement
    ): Op<Boolean>

    fun compileIn(
        column: Column<*>,
        values: List<JsonElement>
    ): Op<Boolean>
}


class PrimitiveFieldDefinition(
    override val type: FieldType,
    private val translateValue: (JsonElement) -> Any
) : FieldDefinition {

    override fun translate(
        value: JsonElement
    ): Any =
        translateValue(value)

    override fun compileComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val translated =
            this@PrimitiveFieldDefinition.translate(value)

        return when (operator) {

            Operator.EQ,
            Operator.NE ->
                typedEquality(
                    column,
                    operator,
                    translated
                )

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE ->
                typedOrdering(
                    column,
                    operator,
                    translated
                )
        }
    }

    override fun compileIn(
        column: Column<*>,
        values: List<JsonElement>
    ): Op<Boolean> {

        val translated =
            values.map(::translate)

        return typedIn(
            column,
            translated
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun typedEquality(
        column: Column<*>,
        operator: Operator,
        value: Any
    ): Op<Boolean> {

        val typedColumn =
            column as Column<Any>

        return when (operator) {

            Operator.EQ ->
                typedColumn eq value

            Operator.NE ->
                typedColumn neq value

            else ->
                error("Not an equality operator: $operator")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun typedOrdering(
        column: Column<*>,
        operator: Operator,
        value: Any
    ): Op<Boolean> {

        val typedColumn =
            column as Column<Comparable<Any>>

        val comparable =
            value as Comparable<Any>

        return when (operator) {

            Operator.GT ->
                typedColumn greater comparable

            Operator.GTE ->
                typedColumn greaterEq comparable

            Operator.LT ->
                typedColumn less comparable

            Operator.LTE ->
                typedColumn lessEq comparable

            else ->
                error("Not an ordering operator: $operator")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun typedIn(
        column: Column<*>,
        values: List<Any>
    ): Op<Boolean> {

        val typedColumn =
            column as Column<Any>

        return typedColumn inList values
    }
}
