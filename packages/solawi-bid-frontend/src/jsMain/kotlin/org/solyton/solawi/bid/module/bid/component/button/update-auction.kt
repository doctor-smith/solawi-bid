package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.text
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.application.ui.page.auction.action.configureAuction
import org.solyton.solawi.bid.module.bid.component.form.showUpdateAuctionModal
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.data.reader.existRounds
import org.solyton.solawi.bid.module.bid.data.rounds
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.user.isNotGranted

@Markup
@Composable
@Suppress("FunctionName")
fun UpdateAuctionButton(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    texts: Reader<Unit, Lang.Block>
) {
    // Auction can only be configured, if no rounds have been created
    val isDisabled = (storage * auction * rounds * existRounds).emit() ||
        (storage * auction * auctionAccepted).emit()||
        (storage * userData.get).emit().isNotGranted(Right.Auction.manage)

    StdButton(
        texts * text,
        storage * deviceData * mediaType.get,
        isDisabled
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
                    actions.emit( configureAuction(auction) )
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
