package org.evoleq.compose.layout

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.compose.style.data.device.compareTo
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun Container(device: Source<DeviceType>, content: @Composable ElementScope<HTMLElement>.()->Unit) {
    Div({
        style {
            if(device.emit() > DeviceType.Tablet) {
                width(80.percent)
                marginLeft(10.percent)
                marginRight(10.percent)
            } else {
                width(98.percent)
                marginLeft(1.percent)
                marginRight(1.percent)
            }
        }
    }) {
        content()
    }
}
