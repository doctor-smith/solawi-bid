package org.evoleq.math

@MathDsl
suspend infix fun <R,S,T> (suspend (S)->T).o(f: suspend (R)->S): suspend (R)->T = {
    r -> this(f(r))
}

@MathDsl
suspend infix fun <S, T> (suspend (S)->T).on(data: S): T = this(data)