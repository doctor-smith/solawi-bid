package org.solyton.solawi.bid.application.data.failure

import org.evoleq.ktorx.result.Result

data class Failure(
    val message: String
)

fun Result.Failure.accept(): Result.Success<Failure> = when(this){
    is Result.Failure.Message -> Result.Success(Failure(value))
    is Result.Failure.Exception -> Result.Success(Failure(value.message?: "No message provided"))
}