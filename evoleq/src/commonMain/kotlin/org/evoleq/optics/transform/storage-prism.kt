package org.evoleq.optics.transform

import org.evoleq.math.on
import org.evoleq.optics.prism.Either
import org.evoleq.optics.prism.Prism
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.add
import org.evoleq.optics.storage.put
import org.evoleq.optics.storage.to
import org.evoleq.optics.storage.write

inline fun <reified Id, reified T> Storage<Map<Id, T>>.prismFromMap(): Prism<Id, T, Id, Pair<Id, T>> = Prism(
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

inline fun <reified T> Storage<List<T>>.prismFromList(): Prism<Int, T, Int, T> = Prism(
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

inline fun <reified T> Storage<List<T>>.firstByOrNull(): Prism<(T)->Boolean, T, Unit, T> = Prism(
    {f -> read().firstOrNull(f)?.let{Either.Right(it)} ?: Either.Left(Unit)},
    {t:T  -> add(t); t }
)

operator fun <W, T> Storage<W>.times(prism: Prism<W, T, Unit, Pair<T, W>>): Storage<T?> =
    Storage (
        read = { when(val match = prism.match(read())){
            is Either.Right -> match.value
            is Either.Left -> null
        } },
        write = {
            t -> when(t){
                null -> Unit
                else -> write(prism.build(t to read())) to this
            }
        }
    )
