package org.solyton.solawi.bid.module.separator

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Markup
@Composable
@Suppress("FunctionName")
fun LineSeparator() = Div(
    attrs = {
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
        }
    }
) {
    Div(
        attrs = {
            style {
                width(100.percent)
                height(2.px)
                backgroundColor(Color.black)
                margin(20.px,0.px)
            }
        }
    )
}