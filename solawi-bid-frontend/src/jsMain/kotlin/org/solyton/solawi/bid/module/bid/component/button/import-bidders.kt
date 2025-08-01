package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.text
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.action.addBidders
import org.solyton.solawi.bid.module.bid.action.importBidders
import org.solyton.solawi.bid.module.bid.component.modal.showImportBiddersModal
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.data.api.AddBidders
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.data.reader.existRounds
import org.solyton.solawi.bid.module.bid.permission.BidRight
import org.solyton.solawi.bid.module.bid.service.isNotGranted
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun ImportBiddersButton(
    storage: Storage<BidApplication>,
    newBidders: Storage<List<NewBidder>>,
    addBidders: Storage<AddBidders>,
    auction: Lens<BidApplication, Auction>,
    texts : Reader<Unit, Lang.Block>,
    dataId: String
) {
    val isDisabled = (storage * auction * rounds * existRounds).emit() ||
        (storage * auction * auctionAccepted).emit()||
        (storage * user.get).emit().isNotGranted(BidRight.Auction.manage)

    StdButton(
        texts * text,
        storage * deviceData * mediaType.get,
        isDisabled,
        dataId
    ) {
        (storage * modals).showImportBiddersModal(
            texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.importBiddersDialog"),
            setBidders = { newBidders.write(it.map { bidder -> bidder.copy(
                username = bidder.username.trim().toLowerCasePreservingASCIIRules()
            )})},
            addBidders = {addBidders.write(AddBidders(it.bidders. map { bidder -> bidder.copy(
                email = bidder.email.trim().toLowerCasePreservingASCIIRules()
            )}))},
            device = storage * deviceData * mediaType.get,
            cancel = {},
            update = {
                CoroutineScope(Job()).launch {
                    (storage * actions).read().dispatch(addBidders(addBidders.read()))
                    (storage * actions).read().dispatch(importBidders(newBidders.read(), auction))
                }
            }
        )
    }
}
