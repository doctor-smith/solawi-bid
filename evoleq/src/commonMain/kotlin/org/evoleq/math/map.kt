package org.evoleq.math

fun <K, V> Map<K, V>.invert(): Map<V, K> = entries.associate { it.value to it.key }
