package org.solyton.solawi.bid.application.ui.page.sendbid

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.ui.style.form.formPageStyle
import org.solyton.solawi.bid.module.bid.action.sendBidAction
import org.solyton.solawi.bid.module.bid.component.SendBidForm
import org.solyton.solawi.bid.module.bid.data.Bid
import org.solyton.solawi.bid.module.bid.data.api.ApiBid

@Markup
@Composable
@Suppress("FunctionName")
fun SendBidPage(storage: Storage<Application>, link: String) = Div(attrs = {style { formPageStyle() }}) {
    SendBidForm {
        CoroutineScope(Job()).launch {
            (storage * actions).read().emit(
                sendBidAction((it to link).toApiType())
            )
        }
    }
}

fun Pair<Bid, String>.toApiType(): ApiBid = ApiBid(first.username, second, first.amount)