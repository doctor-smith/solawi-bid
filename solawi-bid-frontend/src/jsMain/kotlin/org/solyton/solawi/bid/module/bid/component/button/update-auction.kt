package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
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
import org.solyton.solawi.bid.module.bid.action.configureAuction
import org.solyton.solawi.bid.module.bid.component.form.showUpdateAuctionModal
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.data.reader.existRounds
import org.solyton.solawi.bid.module.bid.permission.BidRight
import org.solyton.solawi.bid.module.bid.service.isNotGranted
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun UpdateAuctionButton(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    texts: Reader<Unit, Lang.Block>,
    dataId: String
) {
    // Auction can only be configured, if no rounds have been created
    val isDisabled = (storage * auction * rounds * existRounds).emit() ||
        (storage * auction * auctionAccepted).emit()||
        (storage * user.get).emit().isNotGranted(BidRight.Auction.manage)

    StdButton(
        texts * text,
        storage * deviceData * mediaType.get,
        isDisabled,
        dataId = dataId
    ) {
        (storage * modals).showUpdateAuctionModal(
            auction =  storage * auction,
            texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.updateDialog"),
            device = storage * deviceData * mediaType.get,
            cancel = {}
        ) {
            CoroutineScope(Job()).launch {
                val action = configureAuction(auction)
                val actions = (storage * actions).read()
                try {
                    actions.dispatch( action )
                } catch(exception: Exception) {
                    (storage * modals).showErrorModal(
                        errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action '${action.name}'"),
                        storage * deviceData * mediaType.get

                    )
                }
            }
        }
    }
}
