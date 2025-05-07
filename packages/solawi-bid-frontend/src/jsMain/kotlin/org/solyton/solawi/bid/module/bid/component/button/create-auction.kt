package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.date.today
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.add
import org.evoleq.optics.storage.remove
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.application.ui.page.auction.action.createAuction
import org.solyton.solawi.bid.module.bid.component.form.DEFAULT_AUCTION_ID
import org.solyton.solawi.bid.module.bid.component.form.showAuctionModal
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.user.isNotGranted

@Markup
@Composable
@Suppress("FunctionName")
fun CreateAuctionButton(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    texts: Source<Lang.Block>
) = StdButton(
    texts = texts * title,
    deviceType = storage * deviceData * mediaType.get,
    disabled = (storage * userData.get).emit().isNotGranted(Right.Auction.manage)
){
    // Add auction with dummy id to the store
    ((storage * auctions).add(Auction(auctionId = DEFAULT_AUCTION_ID, "", today())))

    // Show the auction modal
    (storage * modals).showAuctionModal(
        auction = storage * auction,
        texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.createDialog"),
        device = storage * deviceData * mediaType.get,
        cancel = {(storage * auctions).remove { it.auctionId == DEFAULT_AUCTION_ID }}
    ) {
        CoroutineScope(Job()).launch {
            val actions = (storage * actions).read()
            try {
                actions.emit( createAuction(auction) )
            } catch(exception: Exception) {
                (storage * modals).showErrorModal(
                    texts = errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action 'CreateAuction'"),
                    device = storage * deviceData * mediaType.get,
                )
            }
        }
    }
}
