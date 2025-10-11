package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.module.bid.data.i18N
import org.solyton.solawi.bid.module.bid.data.reader.BidComponent
import org.solyton.solawi.bid.module.i18n.data.componentLoaded

@Markup
@Composable
@Suppress("FunctionName")
fun BidRoundEvaluationPage(storage: Storage<Application>, bidRoundId: String) = Div{
    LaunchComponentLookup(
        BidComponent.AuctionPage,
        storage  * Reader { app: Application -> app.environment.useI18nTransform() },
        storage * bidApplicationIso * i18N
    )
    val loaded = (storage * bidApplicationIso * i18N * componentLoaded(BidComponent.AuctionPage)).emit()
    if(!loaded) return@Div


    H1 { Text("BidRoundEvaluationPage, bidRoundId : $bidRoundId") }
    storage.read()

}
