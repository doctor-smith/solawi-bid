package org.evoleq.optics.transform

import org.evoleq.optics.lens.Lens
import org.evoleq.optics.prism.Either
import org.evoleq.optics.prism.Prism
import org.evoleq.optics.prism.multLeft

fun <W, P> Lens<W, P?>.asPrism(): Prism<W, P, Unit, Pair<P, W>> = Prism(
    match = { w ->
        when(val p = get(w)) {
            null -> Either.Left(Unit)
            else -> Either.Right(p)
        }
    },
    build = { (p,w) ->
        set(p)(w)
        p
    }
)

/*
fun <W, P, Q> Prism<W, P, Q, Pair<P, W>>.asLens(): Lens<W, P?> = Lens(
    get = { w -> match(w) },
    set = TODO()
)

 */

operator fun <W, P, Q> Prism<W, P, Unit, Pair<P, W>>.times(lens: Lens<P, Q>): Prism<W, Q, Unit, Pair<Q, W>> = Prism(
    match = { w -> match(w) mapRight { p -> lens.get(p)} },
    build = { (q,w) ->
        match(w) mapRight { p -> lens.set(q)(p) } mapRight { p:P-> build(p to w) }
        q
    }
)
