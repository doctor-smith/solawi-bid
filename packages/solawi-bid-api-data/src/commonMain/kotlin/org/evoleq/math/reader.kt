package org.evoleq.math

typealias Reader<E, T> = (E)->T
typealias Source<T> = Reader<Unit, T>

@MathDsl
fun <E, T> Reader(r: (E)->T): Reader<E, T> = r

@MathDsl
infix fun <E, S, T> Reader<E, S>.map(f: (S)->T): Reader<E, T> = {e -> f (this(e))}

@MathDsl
infix fun <E, T> Reader<E, T>.readFrom(e: E): T = this(e)
@MathDsl
fun <T> Reader<Unit, T>.read(): T = this(Unit)

@MathDsl
fun <T> Source<T>.emit(): T = this(Unit)

@MathDsl
fun <T> Source(emit: ()->T): Source<T> = Reader { emit() }

@MathDsl
operator fun <E, F, T> Reader<E, F>.times(other: Reader<F, T>): Reader<E, T> = this map other

@MathDsl
@Suppress("FunctionName")
fun <T> FirstOrNull(predicate: (T)->Boolean): Reader<List<T>,T?> = Reader{
    it.firstOrNull(predicate)
}
