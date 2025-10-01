package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Markup
@Composable
@Suppress("FunctionName")
fun SimpleUpDown(open: Boolean) = Span {
    Text(if (open) "▲" else "▼")
}
