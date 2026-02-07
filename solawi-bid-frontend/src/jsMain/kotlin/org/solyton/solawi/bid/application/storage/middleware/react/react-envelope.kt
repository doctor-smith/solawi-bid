package org.solyton.solawi.bid.application.storage.middleware.react

import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.x
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.solyton.solawi.bid.application.data.Application

@MathDsl
@Suppress("FunctionName")
fun <S: Any, T: Any> ReactEnvelope(
    envelope: ActionEnvelope<Application>
): KlState<Storage<Application>, Result<T>, Result<T>> = { result ->
    State { storage ->
        val meta = envelope.meta
        val action = envelope.action

        // Beispiel: Nachbearbeitung nur bei Success
        if (result is Result.Success) {
            // hier Meta auswerten und z.B. Prozesse setzen
            // val processId = meta["processId"] as? String
            // if (processId != null && (meta["finishAfterSuccess"] as? Boolean) == true) {
            //    (storage * processes * SetStateOf(processId)) dispatch ProcessState.Finished
            // }

            // Optional: zusätzlich das bisherige action.name-basierte React weiterverwenden
            // (siehe Hinweis unten)
        }

        result x storage
    }
}
