package org.solyton.solawi.bid.application.api

import org.evoleq.ktorx.api.EndPoint
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.Return
import org.evoleq.ktorx.result.apply
import org.evoleq.ktorx.result.on
import org.evoleq.language.Lang
import org.evoleq.math.*
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.state.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.backendPort
import org.solyton.solawi.bid.application.data.env.backendUrl
import org.solyton.solawi.bid.module.error.component.ErrorModal


@MathDsl
@Suppress("FunctionName")
suspend inline fun <S: Any, T: Any> CallApi(action: Action<Application, S, T>) =
    Read<S>(action.reader) *
    Call<S, T>(action) *
    Dispatch<T>(action.writer)


@MathDsl
@Suppress("FunctionName")
fun <T> Read(reader: Reader<Application, T>): State<Storage<Application>, T> = State {
    storage -> (storage * reader).emit() x storage
}

@MathDsl
@Suppress("FunctionName")
fun <S : Any,T : Any> Call(action: Action<Application, S, T>): KlState<Storage<Application>, S, Result<T>> = {
    s -> State{ storage ->
        val application = storage.read()
        val call = (storage * api ).read()[action.endPoint]!!
        val baseUrl = (storage * environment * backendUrl).read()
        val port = (storage * environment * backendPort).read()
        val user = (storage * userData).read()

        val isLoggedIn = user.accessToken != ""
        val url = baseUrl + "/" + call.url

        with(application.client(isLoggedIn)) {
            when(call) {
                is EndPoint.Get -> get<S, T>(url, port, action.serializer, action.deserializer)
                is EndPoint.Post -> post<S, T>(url, port, action.serializer, action.deserializer)
                is EndPoint.Delete -> TODO()//delete<S, T>(url)
                is EndPoint.Head -> TODO()
                is EndPoint.Patch -> TODO()// patch<S, T>(url)

                is EndPoint.Put -> TODO()
            }
        } (s) x storage
    }
}


@MathDsl
@Suppress("FunctionName")
fun <T: Any> Dispatch(writer: Writer<Application, T>): KlState<Storage<Application>, Result<T>, Result<Unit>> = {
    result -> State { storage ->
        console.log("received result: $result")
        when(result) {
            is Result.Success -> Result.Return((storage * writer).dispatch()).apply() on result
            is Result.Failure -> Result.Return(storage.failureWriter().dispatch()).apply() on result
        }
        Result.Return(Unit) x storage
    }
}

fun Storage<Application>.failureWriter(): Writer<Unit, Result.Failure> = {
    failure: Result.Failure -> {
        val message = when (failure) {
            is Result.Failure.Message -> failure.value
            is Result.Failure.Exception -> failure.value.message ?: "No message provided"
        }
        val modals = this * modals
        val nextId = modals.nextId()
        modals.put(
            nextId to ErrorModal(nextId, Lang.Block("", listOf()), modals)
        )
    }
}