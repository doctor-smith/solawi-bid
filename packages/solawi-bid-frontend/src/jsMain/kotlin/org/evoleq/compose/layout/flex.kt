package org.evoleq.compose.layout

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun Flex(content: @Composable ElementScope<HTMLElement>.()->Unit) {
    Div({
        style {
            display(DisplayStyle("flex"))
            flexWrap(FlexWrap.Wrap)
        }
    }) {
        content()
    }
}