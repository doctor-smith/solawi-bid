package org.evoleq.compose.layout

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.dom.Div

@Markup
@Composable
@Suppress("FunctionName")
fun Space(styles: StyleScope.()->Unit = {}) = Div(attrs = {style {
    flexGrow(1)
    styles()
}}) {}