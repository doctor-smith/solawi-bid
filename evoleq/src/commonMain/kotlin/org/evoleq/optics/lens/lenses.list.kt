package org.evoleq.optics.lens

import org.evoleq.math.MathDsl

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
 * Creates a `Lens` for filtering elements within a list based on a given predicate.
 * The resulting `Lens` allows access to the filtered elements or applying transformations
 * that replace the filtered elements with a new list while preserving non-matching elements.
 *
 * @param predicate A filtering function that takes an element of type `T` and returns a
 *                  boolean indicating whether the element satisfies the condition.
 * @return A `Lens` that focuses on elements of a list matching the specified predicate,
 *         enabling functional access and transformation of those elements.
 */
@MathDsl
@Suppress("FunctionName")
fun <T> FilterBy(predicate: (T)-> Boolean): Lens<List<T>,  List<T>> = Lens(
    get = {list -> list.filter(predicate)},
    set = {ts -> {list -> list.filterNot(predicate) + ts}}
)

