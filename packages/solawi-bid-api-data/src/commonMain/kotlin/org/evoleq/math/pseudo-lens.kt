package org.evoleq.math

fun <F, S> Pair<F, S>.first(set: F.()->F): Pair<F, S> = copy(first = first.set())

fun <F, S> Pair<F, S>.second(set: S.()->S): Pair<F, S> = copy(second = second.set())