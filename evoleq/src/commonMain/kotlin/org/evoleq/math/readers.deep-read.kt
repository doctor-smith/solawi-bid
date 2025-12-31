package org.evoleq.math


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

