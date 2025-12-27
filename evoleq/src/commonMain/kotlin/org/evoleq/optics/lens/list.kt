package org.evoleq.optics.lens

import org.evoleq.math.Children
import org.evoleq.math.MathDsl
import org.evoleq.math.Reader

/**
 * Creates a `Lens` for a `List<T>` that focuses on the first element matching the given predicate.
 *
 * This lens allows getting and updating the first element in the list that matches the specified predicate.
 * During updates, the list is reconstructed with the modified value, preserving the order of other elements.
 *
 * @param predicate A function to test each element for a condition.
 * @return A `Lens` that focuses on the first element matching the predicate.
 */
@MathDsl
@Suppress("FunctionName")
fun <T> FirstBy(predicate: (T) -> Boolean): Lens<List<T>, T> {
    var lead: List<T>? = null
    var tail: List<T>? = null
    return Lens(
        get = { list -> list.first(predicate) },
        set = { t ->
            { list ->
                if (lead == null) {
                    lead = list.takeWhile { item -> !predicate(item) }
                }
                if (tail == null) {
                    tail = list.dropWhile { item -> !predicate(item) }.drop(1)
                }
                lead!! + listOf(t) + tail!!
            }
        }
    )
}

/**
 * Searches for an element within a hierarchy of `Children` starting from a given list and traversing
 * depth-first until an element matching the provided predicate is found.
 *
 * @param predicate A function that evaluates each `Children` object and determines whether it matches the condition.
 * @return A `Reader` function that takes a list of `Children` objects and returns the first matching child
 *         of type `T` or `null` if none is found.
 */
@Suppress("FunctionName")
fun <T: Children<T>> DeepRead(predicate: (T) -> Boolean): Reader<List<T>, T?> = {
    list:List<T> -> when{
        list.isEmpty() -> null
        else -> {
            val found = list.firstOrNull(predicate)
            when{
                found != null -> found
                else -> {
                    val newList = list.drop(1).toMutableList()
                    newList.addAll(list.first().getChildren())
                    DeepRead<T>(predicate)(newList)
                }
            }
        }
    }
}

