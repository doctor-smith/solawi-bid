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

/**
 * Displays a chevron icon that toggles between a "down" and "right" orientation
 * based on the provided `open` parameter.
 *
 * @param open A boolean value that determines the orientation of the chevron icon.
 *             When `true`, a "down" chevron is displayed. When `false`, a "right"
 *             chevron is displayed.
 */
@Markup
@Composable
@Suppress("FunctionName")
fun SimpleRightDown(open: Boolean, onClick: () -> Unit = {}) = Span({
    style {
        width(1.em)
        display(DisplayStyle.InlineBlock)
    }
    onClick { onClick() }
}) {
    val icon = if(open) "fa-chevron-down" else "fa-chevron-right"
    I({classes("fa-solid", icon)}){}
}
