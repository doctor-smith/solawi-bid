package org.solyton.solawi.bid.application.storage.middleware.react

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.evoleq.math.dispatch
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.x
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.module.process.data.process.ProcessState
import org.solyton.solawi.bid.module.process.data.processes.SetStateOf
import org.solyton.solawi.bid.module.process.data.processes.processes

@MathDsl
@Suppress("FunctionName")
fun <S: Any, T: Any> ReactEnvelope(
    envelope: ActionEnvelope<Application,*,*>
): KlState<Storage<Application>, Result<T>, Result<T>> = { result ->
    State { storage ->
        // Beispiel: Nachbearbeitung nur bei Success
        if (result is Result.Success) {
            val processId = envelope.id //(meta["processId"] as? String) ?: envelope.action.name
            (storage * processes() * SetStateOf(processId)) dispatch ProcessState.Finished
            val dispatcher = (storage * actions).read()
            for(env in envelope.next) {
                CoroutineScope(Job()).launch {
                    dispatcher dispatchEnvelope env
                }
            }
        }

        result x storage
    }
}
