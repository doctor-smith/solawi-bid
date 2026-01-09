package org.evoleq.math

@MathDsl
@Suppress("FunctionName")
fun <K, V> Get(key: K): Reader<Map<K, V>, V?> = Reader{map -> map[key]}


@MathDsl
@Suppress("FunctionName")
fun <K, V> GetForSure(key: K): Reader<Map<K, V>, V> = Reader{map -> map[key]!!}
