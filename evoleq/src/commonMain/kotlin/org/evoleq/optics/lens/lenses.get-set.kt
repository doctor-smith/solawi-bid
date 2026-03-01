package org.evoleq.optics.lens

import org.evoleq.math.MathDsl
import org.evoleq.math.Reader
import org.evoleq.math.Writer

@MathDsl
@Suppress("FunctionName")
fun <W, P> Get(lens: Lens<W, P>): Reader<W, P> = lens.get

@MathDsl
@Suppress("FunctionName")
fun <W, P> Set(lens: Lens<W, P>): Writer<W, P> = lens.set
