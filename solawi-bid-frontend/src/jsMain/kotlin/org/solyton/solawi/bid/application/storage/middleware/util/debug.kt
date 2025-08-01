package org.solyton.solawi.bid.application.storage.middleware.util

import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.x
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application


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
