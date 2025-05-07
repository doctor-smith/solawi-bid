package org.evoleq.optics.transform

import org.evoleq.optics.prism.Either
import org.evoleq.optics.prism.Prism
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.add
import org.evoleq.optics.storage.put

fun <Id, T> Storage<Map<Id, T>>.prism(): Prism<Id, T, Id, Pair<Id, T>> = Prism(
    {id ->  with(read()[id]){
        when(this){
            null -> Either.Left(id)
            else -> Either.Right(this)
        }
    } },
    {pair ->
        put(pair)
        pair.second
    }
)

inline fun <reified T> Storage<List<T>>.prism(): Prism<Int, T, Int, T> = Prism(
    {index -> try {
        Either.Right(read()[index]!!)
    }catch (exception: Exception) {
        Either.Left(index)
    }},
    { t ->
        add(t)
        t
    }
)