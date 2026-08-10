package org.solyton.solawi.bid.application.ui.page.manual

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.link.Link
import org.evoleq.optics.storage.Storage
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.style.page.Headline
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle

@Composable
@Markup
@Suppress("FunctionName", "UNUSED_PARAMETER")
fun ManualPage(application: Storage<Application>) {
    Page(verticalPageStyle) {
        PageTitle("Betriebsanleitung")

        Headline("Inhaltsverzeichnis")

        Link("Wie man bietet","/manual/how-to-bid")


        Link("Banking Application", "/manual/banking")
    }
}
