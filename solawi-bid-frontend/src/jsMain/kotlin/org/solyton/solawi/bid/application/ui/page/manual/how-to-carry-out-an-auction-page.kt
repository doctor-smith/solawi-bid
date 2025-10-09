package org.solyton.solawi.bid.application.ui.page.manual

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application

@Markup
@Composable
@Suppress("FunctionName", "UNUSED_PARAMETER")
fun HowToCarryOutAnAuctionPage(application: Storage<Application>) {
    Div{
        // todo:i18n
        H1 { Text("How to carry out an auction") }
        // todo:i18n
        Text("To be done")
    }
}
