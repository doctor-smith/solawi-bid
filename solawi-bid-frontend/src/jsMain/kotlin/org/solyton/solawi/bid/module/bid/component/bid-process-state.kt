package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import io.ktor.util.*
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.disabled
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.control.button.ColoredButton
import org.solyton.solawi.bid.module.control.button.StdButton


@Markup
@Composable
@Suppress("FunctionName", "UnusedParameter")
fun BidProcess(texts: Source<Lang.Block>, device: Source<DeviceType>, round: Round, accepted: Boolean = false, action: (RoundState) -> Unit = {}) {
    val roundState: (String) -> Reader<Lang.Block, String> = { name -> Reader { lang: Lang.Block ->
        (lang["states.${name.toLowerCasePreservingASCIIRules()}"])
    } }
    BidProcessState(
        device,
        (texts * roundState(RoundState.Opened.toString())).emit(),
        (texts * roundState(RoundState.Started.toString())).emit(),
        RoundState.Opened.toString(),
        round.state
    ) {
        action(RoundState.Opened)
    }
    BidProcessState(
        device,
        (texts * roundState(RoundState.Started.toString())).emit(),
        (texts * roundState(RoundState.Stopped.toString())).emit(),
        RoundState.Started.toString(),
        round.state
    ){
        action(RoundState.Started)
    }
    BidProcessState(device,
        (texts * roundState(RoundState.Stopped.toString())).emit(),
        (texts * roundState(RoundState.Evaluated.toString())).emit(),
        RoundState.Stopped.toString(),
        round.state
    ) {
        action(RoundState.Stopped)
    }
    BidProcessState(device,
        (texts * roundState(RoundState.Evaluated.toString())).emit(),
        "Show Evaluation",
        RoundState.Evaluated.toString(),
        round.state
    ) {
        action(RoundState.Evaluated)
    }

    /*
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

     */
    //Arrow()
}

@Markup
@Composable
@Suppress("FunctionName")
fun Arrow(device: Source<DeviceType>) = StdButton({"-->"},device ){}

@Markup
@Composable
@Suppress("FunctionName")
fun State(device: Source<DeviceType>, title:String, state: String, currentState: String ) = when(state) {
    currentState -> ColoredButton(Color.seagreen, { title }, device.emit(),) {}
    else -> StdButton({ title }, device.emit(),) {}
}

@Markup
@Composable
@Suppress("FunctionName", "UNUSED_PARAMETER" /* todo:i18n title needs to be injected  */)
fun EndState(device: Source<DeviceType>, title:String, state: String, currentState: String , accepted: Boolean) = when(state) {
    currentState -> when{
        accepted -> ColoredButton(Color.seagreen,{ "Angenommen" }, device.emit(),) {}
        else -> ColoredButton(Color.crimson,{ "Abgelehnt" }, device.emit(),) {}
    }
    else -> StdButton({ "?" }, device.emit(),) {}
}

@Markup
@Composable
@Suppress("FunctionName")
fun BidProcessButton(
    color: CSSColorValue,
    borderColor: CSSColorValue,
    bgColor: CSSColorValue,
    title: Source<String>,
    tooltip: Source<String>,
    isDisabled: Boolean = true,
    action: (SyntheticMouseEvent)->Unit
) {
    val width: Int = 210
    val height: Int = 60
    val stroke: Int = 2
    val tipLength: Int = 20

    Button ({
        if(isDisabled) disabled()
        title(tooltip.emit())
        style {
            background("transparent")
            if(isDisabled) {
                // property("opacity", 0.5)
                cursor("not-allowed")
            } else {
                cursor("pointer")
            }
            color(color)
            //backgroundColor(bgColor)
            property("border", "none")

            position(Position.Relative)
            width(width.px)
            height(height.px)
            backgroundImage("""url("data:image/svg+xml;utf8,
                <svg xmlns='http://www.w3.org/2000/svg' width='$width' height='$height'>
                    <polygon points='0,$stroke 
                        ${width-stroke-tipLength},2
                        ${width-stroke},${height / 2} 
                        ${width-stroke-tipLength},${height - stroke} 
                        0,${height - stroke} 
                        $tipLength,${height / 2}'
                        fill='$bgColor' stroke='$borderColor' stroke-width='$stroke'
                    />
                </svg>")""".trimIndent()
                .replace("\n", "")
                .replace("  ", " ")
            )
            backgroundSize("100% 100%")
            backgroundRepeat("no-repeat")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
        }
        onClick { event -> action(event) }
    }) {
        Text(title.emit())
    }
}

@Markup
@Composable
@Suppress("FunctionName", "UnusedParameter")
fun BidProcessState(device: Source<DeviceType>, title:String, tooltip: String ,state: String, currentState: String, action: ()->Unit ) = when(state) {
    currentState -> BidProcessButton(
        Color.black,
        Color.black,
        Color.seagreen,
        { title },
        { tooltip },
        false
        //device.emit(),
        ) { action() }
    else -> BidProcessButton(
        Color.black,
        Color.black,
        Color.transparent,
        { title },
        { tooltip },
        true
        //device.emit(),
    ) {}
}
