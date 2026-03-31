package org.solyton.solawi.bid.module.dialog.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.style.data.device.DeviceType
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.I

@Markup
@Composable
@Suppress("FunctionName", "UnusedParameter")
fun WarningSymbol(
    color: CSSColorValue = Color.black,
    bgColor: CSSColorValue = Color.transparent,
    deviceType: DeviceType,
    dataId: String? = null,
) = Div({style { maxWidth(100.px)}}) {
    DialogIcon(
        arrayOf("fa-solid", "fa-exclamation-triangle")) {
            color(color)
            backgroundColor(bgColor)
        }

}

@Markup
@Composable
@Suppress("FunctionName")
fun DialogIcon(
    classes: Array<String>,
    styles: StyleScope.()->Unit = {
        width(100.percent);
        height(100.percent)
    }
) {
    Div({style { styles() }}) {
        I({classes(*classes)}){}
    }
}
