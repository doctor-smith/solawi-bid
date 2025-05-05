package org.solyton.solawi.bid.application.api

import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.ktorx.api.EndPoint
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.Return
import org.evoleq.ktorx.result.apply
import org.evoleq.ktorx.result.on
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
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.env.backendPort
import org.solyton.solawi.bid.application.data.env.backendUrl
import org.solyton.solawi.bid.application.data.failure.Failure
import org.solyton.solawi.bid.application.data.failure.accept
import org.solyton.solawi.bid.application.service.seemsToBeLoggerIn
import org.solyton.solawi.bid.module.error.component.ErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts


@MathDsl
@Suppress("FunctionName")
suspend inline fun <S: Any, T: Any> CallApi(action: Action<Application, S, T>) =
    Read<S>(action.reader) *
    Call<S, T>(action)  *
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

        val isLoggedIn = user.seemsToBeLoggerIn()
        val url = baseUrl + "/" + call.url

        with(application.client(isLoggedIn)) {
            when(call) {
                is EndPoint.Get -> get<S, T>(url, port, action.serializer, action.deserializer)
                is EndPoint.Post -> post<S, T>(url, port, action.serializer, action.deserializer)
                is EndPoint.Delete -> delete<S, T>(url, port, action.serializer, action.deserializer)
                is EndPoint.Head -> TODO("Call of function Client.head has not benn implemented yet")
                is EndPoint.Patch -> patch<S, T>(url, port, action.serializer, action.deserializer)

                is EndPoint.Put -> put<S, T>(url, port, action.serializer, action.deserializer)
            }

        } (s) x storage
    }
}


@MathDsl
@Suppress("FunctionName")
fun <T: Any> Dispatch(writer: Writer<Application, T>): KlState<Storage<Application>, Result<T>, Result<T>> = {
    result -> State { storage ->
        when(result) {
            is Result.Success -> Result.Return((storage * writer).dispatch()).apply() on result
            is Result.Failure -> Result.Return((storage.failureWriter()).dispatch()).apply() on result.accept()
        }
        result x storage
    }
}

@MathDsl
@Suppress("FunctionName")
fun <T: Any> Debug(debug: Reader<Application, Unit>): KlState<Storage<Application>, Result<T>, Result<T>> = {
    result -> State { storage ->
        console.log("Debug...")
        (storage * debug).emit()
        console.log("Debug...Done")
    result x storage
    }
}

@MathDsl
@Suppress("FunctionName")
fun <T: Any> DebugResult(debug: Result<T>.()->Unit): KlState<Storage<Application>, Result<T>, Result<T>> = {
    result -> State { storage ->
        console.log("Debug...")
        result.debug()
        console.log("Debug...Done")
        result x storage
    }
}



fun Storage<Application>.failureWriter(): Writer<Unit, Failure> = Writer{
    failure: Failure -> {
        val modals = this * modals
        val nextId = modals.nextId()
        modals.put(
            item = nextId to ModalData(
                ModalType.Error,
                ErrorModal(
                    id = nextId,
                    texts = errorModalTexts(failure.message),
                    modals = modals,

                    device = this@failureWriter * deviceData * mediaType.get,
                )
            )
        )
    }
}
