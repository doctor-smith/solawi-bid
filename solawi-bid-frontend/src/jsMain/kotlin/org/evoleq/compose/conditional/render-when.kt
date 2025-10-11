package org.evoleq.compose.conditional

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun ElementScope<HTMLElement>.When(condition: Boolean, content: @Composable ElementScope<HTMLElement>.()->Unit) {
    if (condition) content()
}
