package org.evoleq.compose.form

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Div


@Markup
@Composable
@Suppress("FunctionName")
fun Form(
    styles: StyleScope.()->Unit,
    content: @Composable ()->Unit
) = Div({style { styles() } }) { content() }
