package org.evoleq.ktorx.result

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapEntrySerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlin.reflect.KClass

/**
 * Store your serializers in a HashMap
 */
val serializers: HashMap<KClass<*>, KSerializer<*>> by lazy { HashMap() }

/**
 * Ease access to serializers
 */
operator fun HashMap<KClass<*>, KSerializer<*>>.get(className: String): KSerializer<*> {
    val clazz = serializers.keys.firstOrNull() { it.simpleName == className }
        ?: Exception("Unregistered serializer")
    return serializers[clazz]!!
}

//@KtorDsl
@Suppress("FunctionName","UNCHECKED_CAST")
inline fun <reified T> Serializer(): KSerializer<T> {
    try {

        return serializers[T::class]!! as KSerializer<T>
    } catch(exception :Exception) {
        print(T::class)
        throw exception
    }
}

fun <T : Any> Serializer(t: T): KSerializer<T> {
    return serializers[t::class]!! as KSerializer<T>
}


@Suppress("FunctionName","UNCHECKED_CAST")
inline fun <reified T : Any> ResultSerializer(): KSerializer<Result<T>> =
    Result.serializer(Serializer())

@Suppress("FunctionName","UNCHECKED_CAST")
inline fun <reified T : Any> ResultListSerializer(): KSerializer<Result<List<T>>> =
    Result.serializer(ListSerializer(Serializer<T>()))

@Suppress("FunctionName","UNCHECKED_CAST")
inline fun <reified K : Any, reified V : Any> ResultMapSerializer(): KSerializer<Result<Map<K, V>>> =
    Result.serializer(MapSerializer(Serializer<K>(), Serializer<V>()))

//@
fun serializers(collect: HashMap<KClass<*>, KSerializer<*>>.()->Unit) {
    serializers.collect()
}


inline fun <reified T> HashMap<KClass<*>, KSerializer<*>>.add(serializer: KSerializer<T>): Unit {
    this[T::class] = serializer
}

fun HashMap<KClass<*>, KSerializer<*>>.add(clazz: KClass<*>, serializer: KSerializer<*>): Unit {
    this[clazz] = serializer
}