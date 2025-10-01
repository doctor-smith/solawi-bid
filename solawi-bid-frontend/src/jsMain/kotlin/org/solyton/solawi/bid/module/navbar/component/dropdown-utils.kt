package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.Composable
import io.ktor.client.fetch.AddEventListenerOptions
import kotlinx.browser.window
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.Event

@Markup
@Composable
@Suppress("FunctionName")
fun SimpleUpDown(open: Boolean) = Span {
    Text(if (open) "▲" else "▼")
}

@Markup
@Composable
fun addDropdownCloseHandler(close: (Event?) -> Unit) {
    window.addEventListener("click",
        { event ->
            close(event)
        },
        object : AddEventListenerOptions {
            override var once: Boolean? = true
        }
    )
}

