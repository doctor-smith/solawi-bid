package org.solyton.solawi.bid.application.storage.middleware

import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.evoleq.math.state.KlState
import org.evoleq.math.state.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.storage.middleware.api.Call
import org.solyton.solawi.bid.application.storage.middleware.react.React
import org.solyton.solawi.bid.application.storage.middleware.react.ReactEnvelope
import org.solyton.solawi.bid.application.storage.middleware.util.Dispatch
import org.solyton.solawi.bid.application.storage.middleware.util.Read

@MathDsl
@Suppress("FunctionName")
suspend inline fun <S: Any, T: Any> ProcessAction(action: Action<Application, S, T>) =
    Read<S>(action.reader) *
    Call<S, T>(action) *
    Dispatch<T>(action.writer) *
    React(action)

@MathDsl
@Suppress("FunctionName", "UNCHECKED_CAST")
suspend inline fun <S: Any, T: Any> ProcessAction(actionEnvelope: ActionEnvelope<Application>)  =
    ProcessAction(actionEnvelope.action as Action<Application, S, T>) *
    ReactEnvelope<S, T>(actionEnvelope)
