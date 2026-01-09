package org.solyton.solawi.bid.module.process.data.process


sealed class ProcessState {
    object Active : ProcessState()

    object Inactive : ProcessState()
    object Finished : ProcessState()

    override fun toString(): String = when(this) {
        Active -> "Active"
        Inactive -> "Inactive"
        Finished -> "Finished"
    }
}

data class IdentifiedProcessState(val id: String, val state: ProcessState)
