package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.Composable
import io.ktor.util.*
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.jetbrains.compose.web.css.Color
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.module.bid.data.Round
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.control.button.ColoredButton
import org.solyton.solawi.bid.module.control.button.StdButton


@Markup
@Composable
@Suppress("FunctionName")
fun BidProcess(texts: Source<Lang.Block>, device: Source<DeviceType>, round: Round, accepted: Boolean = false) {
    val roundState: (String) -> Reader<Lang.Block, String> = { name -> Reader { lang: Lang.Block ->
        (lang["states.${name.toLowerCasePreservingASCIIRules()}"])
    } }

    State(
        device,
        (texts * roundState(RoundState.Opened.toString())).emit(),
        RoundState.Opened.toString(),
        round.state
    )
    Arrow(device)
    State(
        device,
        (texts * roundState(RoundState.Started.toString())).emit(),
        RoundState.Started.toString(),
        round.state
    )
    Arrow(device)
    State(device,
        (texts * roundState(RoundState.Stopped.toString())).emit(),
        RoundState.Stopped.toString(),
        round.state
    )
    Arrow(device)
    State(device,
        (texts * roundState(RoundState.Evaluated.toString())).emit(),
        RoundState.Evaluated.toString(),
        round.state
    )
    Arrow(device)
    EndState(device,
        (texts * roundState(RoundState.Frozen.toString())).emit(),
        RoundState.Frozen.toString(),
        round.state,
        accepted
    )
    //Arrow()
}

@Markup
@Composable
@Suppress("FunctionName")
fun Arrow(device: Source<DeviceType>) = StdButton({"-->"},device ){}
/*Div({
    //classes("button")
    style{
        symbolicButtonStyle(device.emit())()
        backgroundColor(Color.transparent)

        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.Center)

    }
}) {
    I({
        classes("fas", "fa-arrow-right")
        style { fontSize(48.px) }
    })
}
*/

    //StdButton({"-->"},device ){}

@Markup
@Composable
@Suppress("FunctionName")
fun State(device: Source<DeviceType>, title:String, state: String, currentState: String ) = when(state) {
    currentState -> ColoredButton(Color.seagreen, { title }, device.emit(),) {}
    else -> StdButton({ title }, device.emit(),) {}
}

@Markup
@Composable
@Suppress("FunctionName")
fun EndState(device: Source<DeviceType>, title:String, state: String, currentState: String , accepted: Boolean) = when(state) {
    currentState -> when{
        accepted -> ColoredButton(Color.seagreen,{ "Angenommen" }, device.emit(),) {}
        else -> ColoredButton(Color.crimson,{ "Abgelehnt" }, device.emit(),) {}
    }
    else -> StdButton({ "?" }, device.emit(),) {}
}

