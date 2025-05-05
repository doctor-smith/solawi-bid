package org.solyton.solawi.bid.application.ui.page.manual

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.routing.navigate
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.ui.style.page.verticalPageStyle

@Composable
@Markup
@Suppress("FunctionName")
fun ManualPage(application: Storage<Application>) {
    Vertical(verticalPageStyle) {
        H1 { Text("Betriebsanleitung") }

        H2 { "Inhaltsverzeichnis" }

        Horizontal {
            P({onClick { navigate("/manual/how-to-bid") }}){Text("Wie man bietet")}
        }
    }
}

