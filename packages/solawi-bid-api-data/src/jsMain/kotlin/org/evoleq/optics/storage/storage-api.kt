package org.evoleq.optics.storage

import org.evoleq.math.Source
import org.evoleq.math.x


fun <T> Storage<List<T>>.filter(predicate: (T)->Boolean): List<T> = read().filter { predicate(it) }

fun <T> Storage<List<T>>.remove(predicate: (T)->Boolean): Unit = write( read().filter { !predicate(it) } )

fun <T> Storage<List<T>>.onEach(f: (T)->T): Unit = write( read().map(f) )

operator fun <T> Storage<List<T>>.contains(predicate: (T) -> Boolean): Boolean = filter(predicate).isNotEmpty()

operator fun <T> Storage<List<T>>.contains(item: T): Boolean = filter{it == item}.isNotEmpty()

inline fun <reified T> Storage<List<T>>.add(item: T): Unit = write(listOf(
    *read().toTypedArray(),
    item
))

inline fun <reified T> Storage<List<T>>.add(items: List<T>): Unit = write(listOf(
    *read().toTypedArray(),
    *items.toTypedArray()
))

inline fun <reified T, R:Comparable<R>> Storage<List<T>>.sortBy(crossinline f: (T)->R ) {
    write(
        with(arrayListOf(*read().toTypedArray()) ){
            sortBy(f)
            this
        }
    )
}

fun <Id, T> Storage<Map<Id, T>>.readAndFilter(predicate: (Pair<Id, T>)->Boolean): Map<Id,T> = read().filter { s ->  predicate(Pair(s.key, s.value)) }

fun <Id, T> Storage<Map<Id, T>>.put(item: Pair<Id,T>): Unit = write(mapOf(
    *read().map { it.key x it.value }.toTypedArray(),
    item
))

fun <Id, T> Storage<Map<Id, T>>.remove(id: Id): Unit = write(
    with(read().filter { it.key != id }) {
        this
    }
)

fun <Id, T> Storage<Map<Id, T>>.first(predicate: (Pair<Id, T>)->Boolean): T = readAndFilter(predicate).values.first()

fun <Id, T> Storage<Map<Id, T>>.get(id: Id): Source<T> = {
    read()[id]!!
}
fun <T> Storage<Map<Int, T>>.nextId(): Int = read().nextId()


fun <T> Map<Int, T>.nextId(): Int = keys.sorted().nextId()

fun List<Int>.nextId(min: Int = 1): Int = when {
    isEmpty() -> 1
    size == 1 -> when {
        first() == 1 -> 2
        else -> min
    }
    else -> {
        val (f,s) = this
        when {
            s-f > 1 -> f + 1
            else -> drop(1).nextId(s+1)
        }
    }
}
