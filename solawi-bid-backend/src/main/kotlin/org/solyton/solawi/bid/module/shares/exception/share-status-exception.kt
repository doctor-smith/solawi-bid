package org.solyton.solawi.bid.module.shares.exception

import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason

sealed class ShareStatusException(override val message: String): Exception(message) {
    data object NoInitialState : ShareStatusException("No initial State") {
        @Suppress("UnusedPrivateMember")
        private fun readResolve(): Any = NoInitialState
    }
    data class NoSuchStatus(val status: String): ShareStatusException("No such status $status")

    data class NoSuchStatusTransition(val source: String, val target: String): ShareStatusException(
        "No such status transition $source -> $target"
    )

    data class TransitionNotAllowedForModifier(val source: String, val target: String, val modifier: String):ShareStatusException(
        "Transition $source -> $target not allowed for modifier $modifier"
    )
    data class MissingTransitionPermission(val source: String, val target: String, val modifier: String, val reason: String):ShareStatusException(
        "Transition $source -> $target not allowed for modifier $modifier; missing permission $reason"
    )
    data class ForbiddenChangeReason(val reason: ChangeReason): ShareStatusException(
        "forbidden change reason $reason"
    )

    data class InvalidHistoryEntry(override val message: String) : ShareStatusException("Invalid history entry; message = $message")
}
