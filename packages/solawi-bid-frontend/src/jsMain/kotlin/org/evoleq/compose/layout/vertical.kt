package org.evoleq.compose.layout

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun Vertical(style: StyleScope.()->Unit = {}, content: @Composable ElementScope<HTMLElement>.()->Unit) {
    Div({
        style {
            style()
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
        }
    }) {
        content()
    }
}