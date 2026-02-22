package org.solyton.solawi.bid.module.process.data.processes

import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.jetbrains.letsPlot.core.spec.remove
import org.solyton.solawi.bid.module.process.data.process.Process
import org.solyton.solawi.bid.module.process.data.process.ProcessState


val Register: Writer<Processes, Process> = Writer { process -> {processes ->
    processes.registry[process.id] = process//?.let { throw IllegalStateException("Process with id ${process.id} already registered") }

    processes
    // processes.copy(registry = processes.registry + (process.id to process))
}
}

val RegisterIfNotPresent: Writer<Processes, Process> = Writer { process -> {processes ->
    if(!processes.registry.containsKey(process.id)) {
        processes.registry[process.id] = process
    }

    processes
    // processes.copy(registry = processes.registry + (process.id to process))
}
}

val UnRegister: Writer<Processes, String> = Writer { processId -> {processes ->
    processes.registry.remove(processId)
    processes// .copy(registry = processes.registry - processId)
}
}

val UnRegisterAllOf: Writer<Processes, List<String>> = Writer {processIds -> { processes ->
    if(processIds.isNotEmpty()) {
        processes.registry.remove(*processIds.toTypedArray())
    }
    processes// .copy(registry = processes.registry.filterKeys { id -> id !in it })}
} }

val UnRegisterIfNotActive: Writer<Processes, List<String>> = Writer { processIds ->

    { processes ->
        val processesToRemove = processes.registry.filter { entry -> entry.key in processIds && entry.value.state == ProcessState.Active }
        if(processesToRemove.isNotEmpty()) {
            processes.registry.remove(*processesToRemove.keys.toTypedArray())
        }
        processes
        // processes.copy(registry = processes.registry.filter { entry -> entry.key !in it || entry.value.state == ProcessState.Active })
    }
}

@Suppress("FunctionName")
fun SetStateOf(processId: String) : Writer<Processes, ProcessState> = Writer { state -> {processes ->

    val process = processes.registry[processId]//  { throw IllegalStateException("Process with id ${processId} already registered") }

    if(process != null) {
        processes.registry[processId] = process.copy(state = state)
    } else {
        console.warn("Process with id ${processId} not found; tried to set state to $state")
    }
    processes
} }

@Suppress("FunctionName")
fun SetStatesOf(vararg processIds: String) : Writer<Processes, ProcessState> = Writer { state -> {processes ->
    val processMap = processes.registry.filter { entry -> entry.key in processIds }
        .mapValues { proc ->
            proc.value.copy(state = state)
        }
    processes.registry.putAll(processMap)
    processes
} }

/*
val SetStates : Writer<Processes, IdentifiedProcessState> = Writer { iState -> {processes ->
    processes.copy(registry = processes.registry.mapValues { (id, process) ->
        when{
            id == iState.id -> process.copy(state = iState.state)
            else -> process
        }
    })
} }

 */

val IsActive: Reader<Process?, Boolean> = { it?.state == ProcessState.Active }

val IsFinished: Reader<Process?, Boolean> = { it?.state == ProcessState.Finished }

val IsNotFinished: Reader<Process?, Boolean> = { it?.state != ProcessState.Finished }

val IsNotActive: Reader<Process?, Boolean> = { it?.state != ProcessState.Active }

val IsInactive: Reader<Process?, Boolean> = { it?.state == ProcessState.Inactive }


@Suppress("FunctionName")
fun IsRegistered(processId: String): Reader<Processes, Boolean> = { it.registry.containsKey(processId)}

@Suppress("FunctionName")
fun IsNotRegistered(processId: String): Reader<Processes, Boolean> = { !it.registry.containsKey(processId)}
