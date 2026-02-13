package org.solyton.solawi.bid.module.process.data.processes

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.math.Reader
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.solyton.solawi.bid.module.process.data.process.Process


interface ProcessManager<A: Any> {
    val processes: Processes
    val actions: ActionDispatcher<A>

    fun withProcesses(processes: Processes): A
}

@Lensify data class Processes(
    @ReadOnly val registry: Map<String, Process> = mapOf()
)
/*
fun <A: ProcessManager<A>> A.cp(processes: Processes): A {
    require(this.asDynamic().copy != null, { "Cannot copy processes: copy function not found" })
    return this.asDynamic().copy(
        processes = processes
    ) as A
}

 */

fun <A: ProcessManager<A>> processes() : Lens<A, Processes> = Lens(
    get = {a -> a.processes},
    set = { p -> {a -> a.withProcesses(p)}}
)

fun <A: ProcessManager<A>> actions(): Reader<A, ActionDispatcher<A>> = Reader{ a: A -> a.actions }
