package org.solyton.solawi.bid.application.storage.middleware.api

import org.evoleq.ktorx.api.EndPoint
import org.evoleq.ktorx.result.Result
import org.evoleq.math.*
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.api.client
import org.solyton.solawi.bid.application.api.delete
import org.solyton.solawi.bid.application.api.get
import org.solyton.solawi.bid.application.api.patch
import org.solyton.solawi.bid.application.api.post
import org.solyton.solawi.bid.application.api.put
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.backendPort
import org.solyton.solawi.bid.application.data.env.backendUrl
import org.solyton.solawi.bid.application.service.seemsToBeLoggerIn

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
                is EndPoint.Get -> get<S, T>(url, port, action.deserializer)
                is EndPoint.Post -> post<S, T>(url, port, action.serializer, action.deserializer)
                is EndPoint.Delete -> delete<S, T>(url, port, action.serializer, action.deserializer)
                is EndPoint.Head -> TODO("Call of function Client.head has not benn implemented yet")
                is EndPoint.Patch -> patch<S, T>(url, port, action.serializer, action.deserializer)

                is EndPoint.Put -> put<S, T>(url, port, action.serializer, action.deserializer)
            }

        } (s) x storage
    }
}

