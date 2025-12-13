package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.todayWithTime
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.tooltip
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.add
import org.evoleq.optics.storage.remove
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Color
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.module.bid.action.createAuction
import org.solyton.solawi.bid.module.bid.component.form.DEFAULT_AUCTION_ID
import org.solyton.solawi.bid.module.bid.component.form.showAuctionModal
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.biduser.organizations
import org.solyton.solawi.bid.module.bid.permission.BidRight
import org.solyton.solawi.bid.module.bid.service.isNotGranted
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun CreateAuctionButton(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    applicationId: Source<String>,
    texts: Source<Lang.Block>
) = PlusButton(
    color = Color.black,
    bgColor = Color.transparent,
    texts = texts * tooltip,
    deviceType = storage * deviceData * mediaType.get,
    isDisabled = (storage * user.get).emit().isNotGranted(BidRight.Auction.manage),
    dataId = "auctions-page.create-auction-button"
){
    // Add auction with dummy id to the store
    ((storage * auctions).add(Auction(auctionId = DEFAULT_AUCTION_ID, "", todayWithTime())))

    // Show the auction modal
    (storage * modals).showAuctionModal(
        auction = storage * auction,
        organizations = storage * user * organizations.get,
        organizationApplicationContextRelations = storage * applicationOrganizationRelations.get,
        applicationId = applicationId,
        texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.createDialog"),
        device = storage * deviceData * mediaType.get,
        cancel = {(storage * auctions).remove { it.auctionId == DEFAULT_AUCTION_ID }}
    ) {
        CoroutineScope(Job()).launch {
            val actions = (storage * actions).read()
            try {
                actions.dispatch( createAuction(auction) )
            } catch(exception: Exception) {
                (storage * modals).showErrorModal(
                    texts = errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action 'CreateAuction'"),
                    device = storage * deviceData * mediaType.get,
                )
            }
        }
    }
}
