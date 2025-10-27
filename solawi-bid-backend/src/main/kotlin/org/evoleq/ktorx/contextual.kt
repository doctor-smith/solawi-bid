package org.evoleq.ktorx

import java.util.*

data class Contextual<T>(
    val userId: UUID,
    val context: String,
    val data: T
)

/**
 * Functoriality of Contextual
 */
infix fun <S, T> Contextual<S>.map(f: (S)->T): Contextual<T> = Contextual(userId, context, f(data))
