package org.solyton.solawi.bid.module.process.service.middleware

import org.evoleq.math.MathDsl
import org.evoleq.math.dispatch
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.x
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.process.data.process.Process
import org.solyton.solawi.bid.module.process.data.process.ProcessState
import org.solyton.solawi.bid.module.process.data.processes.ProcessManager
import org.solyton.solawi.bid.module.process.data.processes.Register
import org.solyton.solawi.bid.module.process.data.processes.processes


@MathDsl
@Suppress("FunctionName")
fun <T:Any, A: ProcessManager<A>> RegisterProcess(actionEnvelope: ActionEnvelope<A,*,*>): KlState<Storage<A>, T, T> = {
    t: T -> State{ processManager: Storage<A> ->
        val processId = actionEnvelope.id

        (processManager * processes() * Register) dispatch Process(
            id = processId,
            name = actionEnvelope.action.name,
            state = ProcessState.Active
        )
    
        // Activate Process
        // (processManager * processes() * SetStateOf(processId)) dispatch ProcessState.Active
    
        t x processManager
    }
}
