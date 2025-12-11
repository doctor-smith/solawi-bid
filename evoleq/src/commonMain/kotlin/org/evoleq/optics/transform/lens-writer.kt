package org.evoleq.optics.transform

import org.evoleq.math.MathDsl
import org.evoleq.math.Writer
import org.evoleq.math.merge
import org.evoleq.optics.lens.Lens

operator fun <W, P, Q> Lens<W, P>.times(writer: Writer<P, Q>): Writer<W, Q> = this timesW writer

@MathDsl
infix fun <W, P, Q> Lens<W, P>.timesW(writer: Writer<P, Q>): Writer<W, Q> = {
        q -> {w -> set(writer(q)(get(w)))(w)}
}

@MathDsl
inline infix fun <W, reified P> Lens<W, List<P>>.merge(crossinline consideredEqual: (P, P)->Boolean): Writer<W, List<P>> =
    this timesW Writer{inc -> {given -> given.merge(inc,consideredEqual)}}
