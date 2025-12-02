package org.evoleq.compose.dropdown

import androidx.compose.runtime.Composable
import io.ktor.client.fetch.AddEventListenerOptions
import kotlinx.browser.window
import org.evoleq.compose.Markup
import org.w3c.dom.events.Event


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
