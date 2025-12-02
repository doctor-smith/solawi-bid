package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Span

@Markup
@Composable
@Suppress("FunctionName")
fun SimpleUpDown(open: Boolean) = Span({
    style {
        width(1.em)
        display(DisplayStyle.InlineBlock)
    }
}) {
    val icon = if(open) "fa-chevron-left" else "fa-chevron-down"
    I({classes("fa-solid", icon)}){}
}
