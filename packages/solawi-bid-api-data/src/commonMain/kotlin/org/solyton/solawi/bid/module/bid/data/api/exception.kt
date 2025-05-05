package org.solyton.solawi.bid.module.bid.data.api

sealed class RoundStateException(override val message: String) : Exception(message) {
    data class IllegalTransition(val source: RoundState, val target: RoundState): RoundStateException("No such transition $source -> $target")
    data class IllegalRoundState(val state: RoundState): RoundStateException("No such $state")
}