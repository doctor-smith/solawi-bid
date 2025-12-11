package org.evoleq.optics.lens

import org.evoleq.math.Maths
import org.evoleq.math.o
import org.evoleq.math.x as X

@Maths
data class Lens<W, P> (
    val get: (W) -> P,
    val set: (P) -> (W) -> W
)

@Maths
operator fun <W, P, D> Lens<W, P>.times(other: Lens<P, D>): Lens<W, D> = Lens(
    other.get o get) {
    with(other.set(it)) {
        {w:W ->   (set o this o get) (w) (w) }
    }
}


@Maths
fun <V, W, P, Q> Lens<V, P>.x(other: Lens<W, Q>): Lens<Pair<V, W>, Pair<P, Q>> = Lens(
    get X other.get
) {
    pXq -> set(pXq.first) X other.set(pXq.second)
}




@Maths
infix fun <W, P, Q> Lens<W, P>.pX(other: Lens<W, Q>): Lens<W, Pair<P, Q>> = Lens(
    get = {w -> get(w) X other.get(w)}
) {
        pXq -> set(pXq.first) o other.set(pXq.second)
}

@Maths
infix fun <W, P, Q, R> Lens<W, Pair<P, Q>>.tX(other: Lens<W, R>): Lens<W, Triple<P, Q, R>> = Lens(
    get = {w -> with(get(w)){ Triple(first, second, other.get(w))}}
) {
        pXqXr -> set(pXqXr.first X pXqXr.second) o other.set(pXqXr.third)
}

