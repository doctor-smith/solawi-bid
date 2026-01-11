package org.solyton.solawi.bid.module.process.data.processes

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.solyton.solawi.bid.module.process.data.process.IdentifiedProcessState
import org.solyton.solawi.bid.module.process.data.process.Process
import org.solyton.solawi.bid.module.process.data.process.ProcessState

@Lensify data class Processes(
    @ReadOnly val registry: Map<String, Process> = mapOf()
)

val Register: Writer<Processes, Process> = Writer { process -> {processes ->
        processes.copy(registry = processes.registry + (process.id to process))
    }
}

val UnRegister: Writer<Processes, String> = Writer { processId -> {processes ->
        processes.copy(registry = processes.registry - processId)
    }
}

val UnRegisterAllOf: Writer<Processes, List<String>> = Writer { { processes -> processes.copy(registry = processes.registry.filterKeys { id -> id !in it })} }

@Suppress("FunctionName")
fun SetStateOf(processId: String) : Writer<Processes, ProcessState> = Writer { state -> {processes ->
        processes.copy(registry = processes.registry.mapValues { (id, process) ->
            when{
                id == processId -> process.copy(state = state)
                else -> process
            }
    })
} }

@Suppress("FunctionName")
fun SetStatesOf(vararg processIds: String) : Writer<Processes, ProcessState> = Writer { state -> {processes ->
    processes.copy(registry = processes.registry.mapValues { (id, process) ->
        when{
            id in processIds -> process.copy(state = state)
            else -> process
        }
    })

    /*when(state) {
        ProcessState.Finished -> processes.copy(registry = processes.registry.filterKeys { id -> id !in processIds })
        else -> processes.copy(registry = processes.registry.mapValues { (id, process) ->
            when{
                id in processIds -> process.copy(state = state)
                else -> process
            }
        })
    }*/
} }

val SetStates : Writer<Processes, IdentifiedProcessState> = Writer { iState -> {processes ->
    processes.copy(registry = processes.registry.mapValues { (id, process) ->
        when{
            id == iState.id -> process.copy(state = iState.state)
            else -> process
        }
    })
} }

val IsActive: Reader<Process?, Boolean> = { it?.state == ProcessState.Active }

val IsFinished: Reader<Process?, Boolean> = { it?.state == ProcessState.Finished }

val IsNotFinished: Reader<Process?, Boolean> = { it?.state != ProcessState.Finished }

val IsNotActive: Reader<Process?, Boolean> = { it?.state != ProcessState.Active }

val IsInactive: Reader<Process?, Boolean> = { it?.state == ProcessState.Inactive }


@Suppress("FunctionName")
fun IsRegistered(processId: String): Reader<Processes, Boolean> = { it.registry.containsKey(processId)}

@Suppress("FunctionName")
fun IsNotRegistered(processId: String): Reader<Processes, Boolean> = { !it.registry.containsKey(processId)}
