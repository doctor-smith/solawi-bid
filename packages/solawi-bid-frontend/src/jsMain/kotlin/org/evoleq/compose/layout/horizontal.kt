package org.evoleq.compose.layout

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun Horizontal(
    styles: StyleScope.()->Unit = {},
    content: @Composable ElementScope<HTMLElement>.()->Unit
) {
    Div({
        style {
            display(DisplayStyle("flex"))
            styles()
        }
    }) {
        content()
    }
}