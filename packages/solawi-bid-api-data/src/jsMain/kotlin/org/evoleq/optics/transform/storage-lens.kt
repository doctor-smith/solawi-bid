package org.evoleq.optics.transform

import org.evoleq.math.Maths
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage


@Maths
fun <T> Lens<Unit, T>.storage(): Storage<T> = Storage(
    {get(Unit)},
    {t: T -> set(t)(Unit)}
)

@Maths
fun <T> Storage<T>.lens(): Lens<Unit, T> = Lens(
    {read()},
    {t:T  -> {write(t)}}
)

@Maths
operator fun <W, P> Storage<W>.times(lens: Lens<W, P>): Storage<P> = (lens() * lens).storage()
/*
operator fun <S, P, Q> Storage<S>.times(product: Lens<Pair<S, S>, Pair<P, Q>>): Storage<Pair<P, Q>> = Storage(
    read = {},
    write = {}
)

*/