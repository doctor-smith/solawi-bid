package org.evoleq.iql.data



import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import org.evoleq.ktorx.result.add
import kotlin.reflect.KClass

fun iqlSerializers(): Map<KClass<*>, KSerializer<*>> =
    hashMapOf<KClass<*>, KSerializer<*>>().apply {

        // Filters
        add<Filter>(Filter.serializer())
        add<AndFilter>(AndFilter.serializer())
        add<OrFilter>(OrFilter.serializer())
        add<NotFilter>(NotFilter.serializer())
        add<ComparisonFilter>(ComparisonFilter.serializer())
        add<ExpressionComparisonFilter>(ExpressionComparisonFilter.serializer())
        add<InFilter>(InFilter.serializer())
        add<IsNullFilter>(IsNullFilter.serializer())
        add<QuantifierFilter>(QuantifierFilter.serializer())

        // Field references
        add<FieldRef>(FieldRef.serializer())
        add<SimpleFieldRef>(SimpleFieldRef.serializer())
        add<RelationFieldRef>(RelationFieldRef.serializer())

        // Relations
        add<RelationRef>(RelationRef.serializer())

        // Expressions
        add<Expression>(Expression.serializer())
        add<AggregateExpression>(AggregateExpression.serializer())
        add<MapExpression>(MapExpression.serializer())
        add<FilterExpression>(FilterExpression.serializer())
        add<RelationExpression>(RelationExpression.serializer())

        // Aggregation
        add<Aggregation>(Aggregation.serializer())

        // Registry
        add<EntityRegistry>(EntityRegistry.serializer())
        add<EntityType>(EntityType.serializer())
        add<FieldInfo>(FieldInfo.serializer())
        add<RelationInfo>(RelationInfo.serializer())
        add<MappingInfo>(MappingInfo.serializer())

        // Enums / types
        add<Operator>(Operator.serializer())
        add<Quantifier>(Quantifier.serializer())
        add<FieldType>(FieldType.serializer())
        add<RelationType>(RelationType.serializer())

        // Query
        add<Query>(Query.serializer())
        add<Sort>(Sort.serializer())
        add<SortDirection>(SortDirection.serializer())
    }


@OptIn(ExperimentalSerializationApi::class)
val IqlJson = Json {

    classDiscriminator = "type"
    encodeDefaults = true
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = false
    coerceInputValues = false
    allowStructuredMapKeys = false
}
fun Json.configure(
    block: JsonBuilder.() -> Unit
): Json =
    Json(this, block)
