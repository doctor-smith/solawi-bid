package org.evoleq.compose.card

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun Card(
    dataId: String? = null,
    content: @Composable ElementScope<HTMLElement>.()->Unit
) = Div({
    dataId?.let { attr("data-id", it) }
    style{
        border {
            style(LineStyle.Solid)
            width(1.px)
            borderRadius(10.px)
            margin(10.px)
            padding(10.px)
        }
        width(20.pc)
        height(200.px)
    }
}) {
    content()
}
