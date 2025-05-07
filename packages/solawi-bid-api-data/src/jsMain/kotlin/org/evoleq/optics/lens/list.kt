package org.evoleq.optics.lens

import org.evoleq.math.MathDsl

/**
 *
 */
@MathDsl
@Suppress("FunctionName")
fun <T> FirstBy (predicate: (T)->Boolean): Lens<List<T>,T> {
    var lead:Array<T>? = null
    var tail:Array<T>? = null
    return Lens(
    get = {list -> list.first(predicate)},
    set = {t -> {list ->
        if(lead == null){lead = list.takeWhile{item -> !predicate(item)}.toTypedArray()}
        if(tail == null){tail = list.dropWhile { item -> !predicate(item) }.drop(1).toTypedArray()}
        listOf(
            *lead!!,
            t,
            *tail!!
        )
    }}
)}