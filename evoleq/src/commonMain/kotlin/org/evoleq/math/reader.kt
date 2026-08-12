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

infix fun <E,S, T> Reader<E, S>.branch(other: Reader<E, T>): Reader<E, Pair<S,T>> = Reader {e: E -> this(e) to other(e) }

@MathDsl
@Suppress("FunctionName")
fun <T> FirstOrNull(predicate: (T)->Boolean): Reader<List<T>,T?> = Reader{
    it.firstOrNull(predicate)
}

@MathDsl
@Suppress("FunctionName")
fun <T> Size(): Reader<List<T>, Int> = Reader{
    it.size
}

@MathDsl
fun <T> assureValue(): Reader<T?, T> = Reader{value -> require(value != null); value}

@MathDsl
fun <T> not(reader: Reader<T, Boolean>): Reader<T, Boolean> = {t: T -> !reader(t)}

@MathDsl
val negate: Reader<Boolean, Boolean> = Reader{!it}

@MathDsl
infix fun <S, T> Reader<S, Boolean>.or(other: Reader<T, Boolean>): Reader<Pair<S, T>, Boolean> = Reader{
    pair ->  this(pair.first) || other(pair.second)
}

@MathDsl
infix fun <S, T> Reader<S, Boolean>.and(other: Reader<T, Boolean>): Reader<Pair<S, T>, Boolean> = Reader{
    pair ->  this(pair.first) && other(pair.second)
}

@MathDsl
infix fun <S, T> Source<S>.x(other: Source<T>): Source<Pair<S, T>> = Source {
    Pair(emit(), other.emit())
}


operator fun <T> Source<List<T>>.contains(value: T): Boolean =
    emit().contains(value)

@MathDsl
@Suppress("FunctionName")
fun <T> Try(source: Source<T>): Source<T?> = Source<T?> { try {source.emit()} catch (_: Exception) { null } }

@MathDsl
infix fun <T> Source<T?>.onNull(alt: ()->T): Source<T> = Source { emit() ?: alt() }

@MathDsl
infix fun <T> Source<T>.onError(alt: () -> T): Source<T> = Try(this) onNull alt
