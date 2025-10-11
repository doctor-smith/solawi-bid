package org.solyton.solawi.bid.module.page.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Vertical
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun Page(styles: StyleScope.()->Unit, content: @Composable ElementScope<HTMLElement>.()->Unit) = Vertical(styles) {
    content()
}
