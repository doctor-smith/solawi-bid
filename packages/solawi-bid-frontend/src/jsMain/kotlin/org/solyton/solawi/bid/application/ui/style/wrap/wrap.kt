package org.solyton.solawi.bid.application.ui.style.wrap

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement


@Markup
@Composable
@Suppress("FunctionName")
fun Wrap(style: StyleScope.()->Unit = {}, content: @Composable ElementScope<HTMLElement>.()->Unit) = Div({
    style {
        marginBottom(20.px)
        style()
    }
}){
    content()
}