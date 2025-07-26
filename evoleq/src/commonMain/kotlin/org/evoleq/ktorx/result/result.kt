package org.evoleq.ktorx.result

import kotlinx.serialization.Serializable
import org.evoleq.math.MathDsl


@Serializable(with = ResultSerializer::class)
sealed class Result<out T : Any>{
    @Serializable
    data class Success<out T: Any>(@Serializable val data: T): Result<T>()
    @Serializable
    sealed class Failure: Result<Nothing>() {
        @Serializable
        data class Message( val value: String): Failure()
        data class Exception(val value: Throwable): Failure()
    }
}


/**
 * Helper
 */
@MathDsl
infix fun <S, T> ((S)->T).on(value: S):T = this(value)

