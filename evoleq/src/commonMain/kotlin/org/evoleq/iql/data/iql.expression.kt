package org.evoleq.iql.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Expression
@Serializable
@SerialName("field")
data class FieldExpression(
    val field: FieldRef
) : Expression()

@Serializable
@SerialName("relation")
data class RelationExpression(
    val relation: RelationRef
) : Expression()

@Serializable
@SerialName("map")
data class MapExpression(
    val source: Expression,
    val field: FieldRef
) : Expression()

@Serializable
@SerialName("flat_map")
data class FlatMapExpression(
    val source: Expression,
    val expression: Expression
) : Expression()

@Serializable
@SerialName("filter")
data class FilterExpression(
    val source: Expression,
    val filter: Filter
) : Expression()

@Serializable
@SerialName("aggregate")
data class AggregateExpression(
    val source: Expression,
    val aggregation: Aggregation
) : Expression()

@Serializable
enum class Aggregation {
    COUNT,
    MIN,
    MAX,
    SUM,
    AVG
}
