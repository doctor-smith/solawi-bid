package org.evoleq.iql.data



import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import org.evoleq.ktorx.result.add
import kotlin.reflect.KClass

fun iqlSerializers(): Map<KClass<*>, KSerializer<*>> = hashMapOf<KClass<*>, KSerializer<*>>().apply {
    add<Filter>(Filter.serializer())
    add<AndFilter>(AndFilter.serializer())
    add<OrFilter>(OrFilter.serializer())
    add<NotFilter>(NotFilter.serializer())
    add<ComparisonFilter>(ComparisonFilter.serializer())
    add<InFilter>(InFilter.serializer())
    add<IsNullFilter>(IsNullFilter.serializer())
    add<QuantifierFilter>(QuantifierFilter.serializer())
    add<SimpleFieldRef>(SimpleFieldRef.serializer())
    add<RelationFieldRef>(RelationFieldRef.serializer())
    add<RelationRef>(RelationRef.serializer())
    add<EntityRegistry>(EntityRegistry.serializer())
    add<EntityType>(EntityType.serializer())
    add<FieldInfo>(FieldInfo.serializer())
    add<RelationInfo>(RelationInfo.serializer())
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
