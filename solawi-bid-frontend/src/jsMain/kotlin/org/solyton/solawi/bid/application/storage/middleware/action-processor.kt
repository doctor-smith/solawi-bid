package org.solyton.solawi.bid.application.storage.middleware

import org.evoleq.math.MathDsl
import org.evoleq.math.state.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.ActionEnvelope
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.storage.middleware.api.Call
import org.solyton.solawi.bid.application.storage.middleware.react.React
import org.solyton.solawi.bid.application.storage.middleware.react.ReactEnvelope
import org.solyton.solawi.bid.application.storage.middleware.util.Dispatch
import org.solyton.solawi.bid.application.storage.middleware.util.Read
import org.solyton.solawi.bid.module.process.service.middleware.RegisterProcess

@MathDsl
@Suppress("FunctionName")
suspend inline fun <S: Any, T: Any> ProcessAction(action: Action<Application, S, T>) =
    Read<S>(action.reader) *
    Call<S, T>(action) *
    Dispatch<T>(action.writer) *
    React(action)

@MathDsl
@Suppress("FunctionName", "UNCHECKED_CAST")
suspend inline fun <S: Any, T: Any> ProcessAction(actionEnvelope: ActionEnvelope<Application,S,T>)  =
    Read<S>((actionEnvelope.action as Action<Application, S, T>).reader) *
    RegisterProcess<S, Application>(actionEnvelope) *
    Call<S, T>(actionEnvelope.action as Action<Application, S, T>) *
    Dispatch<T>((actionEnvelope.action  as Action<Application, S, T>).writer) *
    React(actionEnvelope.action as Action<Application, S, T>) *
    ReactEnvelope<S, T>(actionEnvelope)
