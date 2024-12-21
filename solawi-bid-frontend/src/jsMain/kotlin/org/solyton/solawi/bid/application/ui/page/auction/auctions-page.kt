package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.date.today
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.add
import org.evoleq.optics.storage.remove
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.ui.page.auction.action.createAuction
import org.solyton.solawi.bid.application.ui.page.auction.action.readAuctions
import org.solyton.solawi.bid.module.bid.component.AuctionList
import org.solyton.solawi.bid.module.bid.component.DEFAULT_AUCTION_ID
import org.solyton.solawi.bid.module.bid.component.showAuctionModal
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun AuctionsPage(storage: Storage<Application>) = Div{

    LaunchedEffect(Unit) {
        (storage * actions).read().emit(readAuctions())
    }

    val auction = auctions * FirstBy{ it.auctionId == DEFAULT_AUCTION_ID }

    Button(attrs = {
        onClick {
            ((storage * auctions).add(Auction(auctionId= DEFAULT_AUCTION_ID, "", today())))
            (storage * modals).showAuctionModal(
                auction = storage * auction,
                texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.createDialog"),
                cancel = {(storage * auctions).remove { it.auctionId == DEFAULT_AUCTION_ID }}
            ) {
                CoroutineScope(Job()).launch {
                    val actions = (storage * actions).read()
                    try {
                        actions.emit( createAuction(auction) )
                    } catch(exception: Exception) {
                        (storage * modals).showErrorModal(
                            errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action 'CreateAuction'")
                        )
                    }
                }
            }
        }
    } ){
        Text("Create Auction")
    }
    // render list of auctions
    // each entry offers the opportunity to read details, edit, or delete
    AuctionList(storage * auctions){
        CoroutineScope(Job()).launch {
            val actions = (storage * actions).read()
            try {
                actions.emit( it )
            } catch(exception: Exception) {
                (storage * modals).showErrorModal(
                    errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action '${it.name}'")
                )
            }
        }
    }
}
