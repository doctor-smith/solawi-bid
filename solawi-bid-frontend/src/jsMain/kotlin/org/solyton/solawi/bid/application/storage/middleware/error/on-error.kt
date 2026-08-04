package org.solyton.solawi.bid.application.storage.middleware.error

import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.ktorx.context.data.Contextual
import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.evoleq.math.dispatch
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.x
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application

@MathDsl
@Suppress("FunctionName")
fun <S: Any, T : Any> OnError(action: Action<Application, S, T>): KlState<Storage<Application>, Result<Contextual<T>>, Result<Contextual<T>>> = { result ->
    State{ storage ->
        println(result)
        println(action.failOnError)
        when(result) {
            is Result.Success<*> -> result
            is Result.Failure -> handleFailure(result, storage, action)
        }  x storage
    }
}

fun <S: Any, T : Any> handleFailure(result: Result.Failure, storage: Storage<Application>, action: Action<Application, S, T>): Result<Contextual<T>> = when(result) {
    is Result.Failure.HttpStatusMessage -> {
        if(action.onError != null) CoroutineScope(Job()).launch {
            val onError = requireNotNull(action.onError) {"Action ${action.name} breaks - onError should not be null"}
            (storage * onError) dispatch (result.statusCode x result.value)
        }
        when{
            action.failOnError -> result
            else -> result.copy(statusCode = HttpStatusCode.OK)
        }
    }
    else -> result as Result<Contextual<T>>
}
