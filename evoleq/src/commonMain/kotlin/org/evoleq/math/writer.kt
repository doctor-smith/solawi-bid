package org.evoleq.math

typealias Writer<W, P> = (P)->(W)->W
typealias Dispatcher<P> = Writer<Unit, P>

@MathDsl
@Suppress("FunctionName")
fun <W, P> Writer(writer: (P)->(W)->W) = writer

@MathDsl
infix fun <W, Q, P> Writer<W, P>.contraMap(f: (Q)->P): Writer<W, Q> = {q -> this(f(q))}

@MathDsl
infix fun <W, P> Writer<W, P>.write(p: P): (W) -> W = this(p)

@MathDsl
infix fun <W> ((W)->W).on(w: W): W = this(w)

@MathDsl
infix fun <W> Writer<Unit, W>.dispatch(w : W): Unit = write(w) on Unit

@MathDsl
fun <W> Dispatcher<W>.dispatch(): (w: W)->Unit = {w: W -> dispatch(w)}
