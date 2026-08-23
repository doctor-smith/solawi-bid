
package org.evoleq.exposedx.iql

import kotlinx.serialization.json.JsonElement
import org.evoleq.iql.data.FieldType
import org.evoleq.iql.data.Operator
import org.evoleq.iql.dsl.LIKE_ESCAPE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq

interface FieldDefinition {

    val type: FieldType

    fun translate(
        value: JsonElement
    ): Any

    /**
     * Existing column-based API.
     */
    fun compileComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> =
        compileComparison(
            expression = column,
            operator = operator,
            value = value
        )

    /**
     * Expression-based comparison.
     *
     * A Column is an Expression, but aggregates and calculated expressions
     * are not Columns. This overload allows both to use the same
     * type-aware comparison implementation.
     */
    /*
    fun compileComparison(
        expression: Expression<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val translated =
            translate(value)

        return when (operator) {

            Operator.EQ,
            Operator.NE ->
                typedEquality(
                    expression = expression,
                    operator = operator,
                    value = translated
                )

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE ->
                typedOrdering(
                    expression = expression,
                    operator = operator,
                    value = translated
                )

            Operator.LIKE ->
                typedLike(
                    expression = expression,
                    value = translated,
                    ignoreCase = false
                )
        }
    }
    */
    fun compileComparison(
        expression: ExpressionWithColumnType<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val translated =
            translate(value)

        return when (operator) {

            Operator.EQ,
            Operator.NE ->
                typedEquality(
                    expression,
                    operator,
                    translated
                )

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE ->
                typedOrdering(
                    expression,
                    operator,
                    translated
                )

            Operator.LIKE ->
                typedLike(
                    expression,
                    translated,
                    false
                )
        }
    }

    fun compileIn(
        column: Column<*>,
        values: List<JsonElement>
    ): Op<Boolean> {

        val translated =
            values.map(::translate)

        return typedIn(
            column = column,
            values = translated
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun typedEquality(
        expression: Expression<*>,
        operator: Operator,
        value: Any
    ): Op<Boolean> {

        val typedExpression =
            expression as Expression<Any>

        return when (operator) {

            Operator.EQ ->
                typedExpression eq value as ExpressionWithColumnType<EntityID<*>?>

            Operator.NE ->
                typedExpression neq value as ExpressionWithColumnType<EntityID<*>?>

            else ->
                error(
                    "Not an equality operator: $operator"
                )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun typedOrdering(
        expression: Expression<*>,
        operator: Operator,
        value: Any
    ): Op<Boolean> {

        val typedExpression =
            expression as Expression<Comparable<Any>>

        val comparable =
            value as ExpressionWithColumnType<EntityID<Comparable<Any>>?>

        return when (operator) {

            Operator.GT ->
                typedExpression greater comparable

            Operator.GTE ->
                typedExpression greaterEq comparable

            Operator.LT ->
                typedExpression less comparable

            Operator.LTE ->
                typedExpression less comparable

            else ->
                error(
                    "Not an ordering operator: $operator"
                )
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

    @Suppress("UNCHECKED_CAST")
    private fun typedEquality(
        expression: ExpressionWithColumnType<*>,
        operator: Operator,
        value: Any
    ): Op<Boolean> {

        val typedExpression =
            expression as ExpressionWithColumnType<Any>

        return when (operator) {

            Operator.EQ ->
                typedExpression eq value

            Operator.NE ->
                typedExpression neq value

            else ->
                error("Not an equality operator: $operator")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun typedOrdering(
        expression: ExpressionWithColumnType<*>,
        operator: Operator,
        value: Any
    ): Op<Boolean> {

        val typedExpression =
            expression as ExpressionWithColumnType<Comparable<Any>>

        val comparable =
            value as Comparable<Any>

        return when (operator) {

            Operator.GT ->
                typedExpression greater comparable

            Operator.GTE ->
                typedExpression greaterEq comparable

            Operator.LT ->
                typedExpression less comparable

            Operator.LTE ->
                typedExpression lessEq comparable

            else ->
                error("Not an ordering operator: $operator")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun typedLike(
        expression: ExpressionWithColumnType<*>,
        value: Any?,
        ignoreCase: Boolean
    ): Op<Boolean> =

        when (value) {

            is String -> {

                val typedExpression =
                    expression as ExpressionWithColumnType<String>

                if (ignoreCase) {

                    typedExpression
                        .lowerCase()
                        .like(
                            LikePattern(
                                value.lowercase(),
                                LIKE_ESCAPE
                            )
                        )

                } else {

                    typedExpression.like(
                        LikePattern(
                            value,
                            LIKE_ESCAPE
                        )
                    )
                }
            }

            else ->
                error("Not a string: $value")
        }
}

class PrimitiveFieldDefinition(
    override val type: FieldType,
    private val translateValue: (JsonElement) -> Any
) : FieldDefinition {

    override fun translate(
        value: JsonElement
    ): Any =
        translateValue(value)
}
