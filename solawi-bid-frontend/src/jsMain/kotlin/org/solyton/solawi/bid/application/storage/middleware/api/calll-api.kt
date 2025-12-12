package org.solyton.solawi.bid.application.storage.middleware.api

import org.evoleq.ktorx.api.EndPoint
import org.evoleq.ktorx.client.*
import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.x
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.api.*
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.api
import org.solyton.solawi.bid.application.data.env.backendPort
import org.solyton.solawi.bid.application.data.env.backendUrl
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.application.service.seemsToBeLoggerIn
import org.evoleq.ktorx.context.data.Contextual

@MathDsl
@Suppress("FunctionName")
fun <S : Any,T : Any> Call(action: Action<Application, S, T>): KlState<Storage<Application>, S, Result<Contextual<T>>> = {
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

