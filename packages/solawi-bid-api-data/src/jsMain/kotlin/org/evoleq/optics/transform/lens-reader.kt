package org.evoleq.optics.transform

import org.evoleq.math.MathDsl
import org.evoleq.math.Reader
import org.evoleq.math.map
import org.evoleq.optics.lens.Lens

operator fun <W, Q, P> Lens<W, Q>.times(reader: Reader<Q, P>): Reader<W, P> = this timesR reader

@MathDsl
infix fun <W, Q, P> Lens<W, Q>.timesR(reader: Reader<Q, P>): Reader<W, P> = get map reader