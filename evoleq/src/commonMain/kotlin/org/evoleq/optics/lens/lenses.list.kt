package org.evoleq.optics.lens

import org.evoleq.math.MathDsl

/**
 * A partial focus on the first matching list element.
 *
 * Each read/write searches the list provided to that call. No mutable state is kept in the
 * lens, so it can safely be reused with different lists.
 *
 * This is not a total, universally lawful lens: a matching element must exist, and replacing
 * it with a value that no longer satisfies [predicate] can invalidate Put-Get on subsequent
 * reads. Use it only when the caller can guarantee a stable matching focus.
 *
 * @throws NoSuchElementException if no list element matches [predicate].
 */
@MathDsl
@Suppress("FunctionName")
fun <T> FirstBy(predicate: (T) -> Boolean): Lens<List<T>, T> = Lens(
    get = { list -> list.first(predicate) },
    set = { replacement ->
        { list ->
            val index = list.indexOfFirst(predicate)
            if (index < 0) {
                throw NoSuchElementException("No list element matches the FirstBy predicate")
            }
            list.mapIndexed { position, item ->
                if (position == index) replacement else item
            }
        }
    }
)

/**
 * An explicit, order-changing selection/edit operation on a list, NOT a lawful Lens.
 *
 * [select] returns the elements matching the predicate. [replaceWith] removes all matching
 * elements and appends replacements after the untouched non-matching elements. In particular,
 * replacing a selection with itself may reorder a list; this type intentionally does not
 * implement [LensType] or claim lens laws.
 */
class ListSelection<T>(private val predicate: (T) -> Boolean) {

    fun select(source: List<T>): List<T> = source.filter(predicate)

    fun replaceWith(source: List<T>, replacement: List<T>): List<T> =
        source.filterNot(predicate) + replacement
}

/**
 * Select and replace elements matching [predicate], preserving the prior replacement semantics
 * but without falsely claiming the lens laws.
 *
 * This changes the return type from Lens<List<T>, List<T>> to [ListSelection]. Callers should
 * use `selection.select(list)` and `selection.replaceWith(list, replacement)`.
 */
@MathDsl
@Suppress("FunctionName")
fun <T> FilterBy(predicate: (T) -> Boolean): ListSelection<T> = ListSelection(predicate)

@MathDsl
@Suppress("FunctionName")
fun <S, T> BiMap(forth: (S) -> T, back: (T)->S): Lens<List<S>, List<T>> = Lens(
    get = {list -> list.map(forth)},
    set = {ts -> {_ -> ts.map(back)}}
)

