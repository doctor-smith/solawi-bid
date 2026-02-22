package org.solyton.solawi.bid.module.process.service.process

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.evoleq.math.MathDsl
import org.evoleq.math.emit
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.process.data.process.Process
import org.solyton.solawi.bid.module.process.data.process.ProcessState
import org.solyton.solawi.bid.module.process.data.processes.ProcessManager
import org.solyton.solawi.bid.module.process.data.processes.actions

/**
 * Runs a single process if it's not already registered or not finished
 * @param setup Factory function that creates the process envelope
 * @return true if the process was started or is still running, false if finished
 */
@MathDsl
fun <A : ProcessManager<A>> Storage<A>.runProcess(
    scope: CoroutineScope,
    setup: () -> ActionEnvelope<A, *, *>
): Boolean {
    val registry = this.read().processes.registry
    val envelope = setup()
    val registeredProcess = registry[envelope.id]
    if (registeredProcess == null) {
        registry.put(envelope.id, Process(
            id = envelope.id,
            name = envelope.action.name,
            state = ProcessState.Registered
        ))
        scope.launch {
            ((this@runProcess * actions()).emit() dispatchEnvelope setup())
        }
        return true
    }
    return registeredProcess.state != ProcessState.Finished
}

@MathDsl
@Composable
fun <A : ProcessManager<A>> Storage<A>.runProcess(
    setup: () -> ActionEnvelope<A, *, *>
): Boolean {
    val state by produceState(initialValue = true) {
        val registry = this@runProcess.read().processes.registry
        val envelope = setup()
        val registeredProcess = registry[envelope.id]
        if (registeredProcess == null) {
            registry.put(envelope.id, Process(
                id = envelope.id,
                name = envelope.action.name,
                state = ProcessState.Registered
            ))
            (this@runProcess * actions()).emit() dispatchEnvelope setup()
        }
        value = false
    }
    return state
}

/**
 * Runs multiple processes in parallel
 * @param processes Process envelopes to run
 * @return Array of boolean values indicating status of each process
 */
@MathDsl
fun <A : ProcessManager<A>> Storage<A>.runProcesses(scope: CoroutineScope, vararg processes: ActionEnvelope<A, *, *>): BooleanArray {
    return processes.filter { it.run }.map { envelope ->  runProcess(scope) { envelope } }.toBooleanArray()
}

@MathDsl
@Composable
fun <A: ProcessManager<A>> Storage<A>.runProcesses(vararg processes: ActionEnvelope<A, *, *>):  BooleanArray {
    return processes.filter { it.run }.map { envelope ->  runProcess { envelope } }.toBooleanArray()
}

/**
 * Runs multiple processes in sequence
 * @param first First process to run
 * @param processes Subsequent processes to run in order
 * @return true if the sequence was started or is running, false if finished
 */
@MathDsl
fun <A : ProcessManager<A>> Storage<A>.sequenceProcesses(
    scope: CoroutineScope,
    vararg processes: ActionEnvelope<A, *, *>
): Boolean {
    val processesToRun = processes.takeWhile { it.run }
    if (processesToRun.isEmpty()) return false
    return runProcess(scope) { sequence( * processesToRun.toTypedArray()) }
}

/**
 * Creates a sequence of processes by chaining their next references
 * @param processes Processes to chain together
 * @return First process envelope in the chain
 * @throws IllegalArgumentException if the processes array is empty
 */
@MathDsl
fun <A : ProcessManager<A>> sequence(vararg processes: ActionEnvelope<A, *, *>): ActionEnvelope<A, *, *> {
    require(processes.isNotEmpty()) { "Processes must not be empty" }
    return processes.dropLast(1).foldRight(processes.last()) { envelope, acc ->
        envelope.copy(next = listOf(acc))
    }
}

/**
 * Extension to add next steps to an envelope.
 *
 * @param processes Vararg parameter representing the additional `ActionEnvelope`s to be added
 *                  to the `next` list of this envelope.
 * @return A new `ActionEnvelope` instance with the updated `next` list containing the current
 *         envelope's `next` entries and the provided `processes`.
 */
@MathDsl
fun <A : ProcessManager<A>> ActionEnvelope<A, *, *>.next(vararg processes: ActionEnvelope<A, *, *>): ActionEnvelope<A, *, *> =
    copy(next = this.next + processes.toList())

@MathDsl
fun <A : ProcessManager<A>> ActionEnvelope<A, *, *>.nextLazy(vararg processes: ()->ActionEnvelope<A, *, *>): ActionEnvelope<A, *, *> =
    copy(nextLazy = this.nextLazy + processes.toList())

