package org.evoleq.ktorx.result

import kotlinx.coroutines.coroutineScope
import org.evoleq.math.MathDsl


/**
 * Functoriality
 */
@MathDsl
suspend infix fun <S: Any,T: Any> Result<S>.mapSuspend(f: suspend (S)->T): Result<T> = coroutineScope {
    when(this@mapSuspend) {
        is Result.Success<S> -> try{
            Result.Success(f(data))
        } catch (e:Throwable) {
            Result.Failure.Exception(e)//.message?: "No message provided; source ${e::class}")
        }
        is Result.Failure -> when(this@mapSuspend){
            is Result.Failure.Message -> Result.Failure.Message(value)
            is Result.Failure.Exception -> Result.Failure.Exception(value)
        }
    } }
/**
 * Monad Return
 */
/*
@Suppress("FunctionName")
@MathDsl
fun <T : Any> Result.Companion.Return(data: T): Result<T> = Result.Success(data)
*/
/*

operator fun <T : Any> Result.Companion.invoke(data: T): Result<T> = Result.Success(data)
*/

/**
 * Monad multiplication
 */
@MathDsl
suspend fun <T: Any> Result<Result<T>>.multiplySuspend(): Result<T> = when(this) {
    is Result.Success -> data
    is Result.Failure -> this
}

/**
 * Monad bind
 */
@MathDsl
suspend infix fun <S:Any, T:Any> Result<S>.bindSuspend(f: suspend (S) -> Result<T>): Result<T> = (this mapSuspend  f).multiply()

/**
 * Applicative
 */
@MathDsl
suspend fun <S:Any, T:Any> Result<suspend (S)->T>.apply(): suspend (Result<S>)->Result<T> = {
        rS -> this bindSuspend  {f -> rS mapSuspend  f}
}

@MathDsl
suspend fun Result<suspend (Unit)->Unit>.dispatchSuspend(): Result<Unit> = mapSuspend { it(Unit) }
