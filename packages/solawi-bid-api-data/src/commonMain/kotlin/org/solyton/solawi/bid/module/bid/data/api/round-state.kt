package org.solyton.solawi.bid.module.bid.data.api

interface Command {
    val commandName: String
}

sealed class RoundState : Command {
    data object Opened : RoundState() {
        override val commandName: String = "Start"
        override fun toString(): String = "OPENED"
    }
    data object Started : RoundState(){
        override val commandName: String = "Stop"
        override fun toString(): String = "STARTED"
    }
    data object Stopped : RoundState(){
        override val commandName: String = "Evaluate"
        override fun toString(): String = "STOPPED"
    }
    data object Evaluated: RoundState(){
        override val commandName: String = "Close"
        override fun toString(): String = "EVALUATED"
    }
    data object Closed: RoundState(){
        override val commandName: String = "Freeze"
        override fun toString(): String = "CLOSED"
    }
    data object Frozen: RoundState(){
        override val commandName: String = "Null"
        override fun toString(): String = "FROZEN"
    }


    companion object {
        fun fromString(state: String): RoundState = when(state){
            RoundState.Opened.toString() -> RoundState.Opened
            RoundState.Started.toString() -> RoundState.Started
            RoundState.Stopped.toString() -> RoundState.Stopped
            RoundState.Evaluated.toString() -> RoundState.Evaluated
            RoundState.Closed.toString() -> RoundState.Closed
            RoundState.Frozen.toString() -> RoundState.Frozen
            else -> throw Exception("No such RoundState")
        }
    }

}

fun RoundState.nextState(): RoundState = when(this) {
    RoundState.Opened -> RoundState.Started
    RoundState.Started -> RoundState.Stopped
    RoundState.Stopped -> RoundState.Evaluated
    RoundState.Evaluated -> RoundState.Closed
    RoundState.Closed -> RoundState.Frozen
    RoundState.Frozen -> RoundState.Frozen
}

val roundStates: List<RoundState> = listOf(
    RoundState.Opened,
    RoundState.Started,
    RoundState.Stopped,
    RoundState.Closed,
    RoundState.Evaluated,
    RoundState.Frozen
)

val roundStateNames: List<String> = roundStates.map { it.toString() }

fun String.isValidRoundStateName(): Boolean = roundStateNames.contains(this)