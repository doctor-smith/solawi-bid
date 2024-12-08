package org.evoleq.math

@MathDsl
fun <T> List<T>.dropFirst(): Pair<T?, List<T>> = when {
    isEmpty() -> Pair(null, this)
    else -> Pair(first(), drop(1))
}

/**
 * Merge one list into another.
 * Note: Data of the left side will be overwritten!!
 */
inline fun <reified T> List<T>.merge(other:List<T>, consideredEqual: (T, T)->Boolean): List<T> = listOf(
    *filter { item -> other.none{ jtem -> consideredEqual(item, jtem) } }.toTypedArray(),
    *other.toTypedArray()
)
