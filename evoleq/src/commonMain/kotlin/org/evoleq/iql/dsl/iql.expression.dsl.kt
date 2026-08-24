package org.evoleq.iql.dsl

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.iql.data.*

fun expression(path: String): Expression =
    FieldExpression(
        SimpleFieldRef(path)
    )

fun relation(
    relation: String,
    entity: String
): Expression =
    RelationExpression(
        RelationRef(
            entity = entity,
            relation = relation
        )
    )

fun relation(
    relation: String
): Expression =
    RelationExpression(
        RelationRef(
            entity = "",
            relation = relation
        )
    )

infix fun Expression.eq(
    value: JsonElement
): Filter =
    ExpressionComparisonFilter(
        expression = this,
        operator = Operator.EQ,
        value = value
    )

infix fun Expression.eq(value: String): Filter =
    ExpressionComparisonFilter(
        expression = this,
        operator = Operator.EQ,
        value = JsonPrimitive(value)
    )

fun Expression.map(
    field: String
): MapExpression =
    MapExpression(
        source = this,
        field = SimpleFieldRef(field)
    )

infix fun Expression.eq(value: Int): Filter =
    ExpressionComparisonFilter(
        expression = this,
        operator = Operator.EQ,
        value = JsonPrimitive(value)
    )

infix fun Expression.eq(value: Long): Filter =
    ExpressionComparisonFilter(
        expression = this,
        operator = Operator.EQ,
        value = JsonPrimitive(value)
    )

infix fun Expression.eq(value: Boolean): Filter =
    ExpressionComparisonFilter(
        expression = this,
        operator = Operator.EQ,
        value = JsonPrimitive(value)
    )

fun Expression.min(): AggregateExpression =
    AggregateExpression(
        source = this,
        aggregation = Aggregation.MIN
    )

fun Expression.max(): AggregateExpression =
    AggregateExpression(
        source = this,
        aggregation = Aggregation.MAX
    )

fun Expression.sum(): AggregateExpression =
    AggregateExpression(
        source = this,
        aggregation = Aggregation.SUM
    )

fun Expression.avg(): AggregateExpression =
    AggregateExpression(
        source = this,
        aggregation = Aggregation.AVG
    )

fun Expression.count(): AggregateExpression =
    AggregateExpression(
        source = this,
        aggregation = Aggregation.COUNT
    )
