package org.solyton.solawi.bid.application.storage.middleware

import org.evoleq.math.MathDsl
import org.evoleq.math.state.times
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.storage.middleware.api.Call
import org.solyton.solawi.bid.application.storage.middleware.util.Dispatch
import org.solyton.solawi.bid.application.storage.middleware.util.Read
import org.solyton.solawi.bid.application.storage.middleware.react.React

@MathDsl
@Suppress("FunctionName")
suspend inline fun <S: Any, T: Any> ProcessAction(action: Action<Application, S, T>) =
    Read<S>(action.reader) *
            Call<S, T>(action) *
            Dispatch<T>(action.writer) *
    React(action)
