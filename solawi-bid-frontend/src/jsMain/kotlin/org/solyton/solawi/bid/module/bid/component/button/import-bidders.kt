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
import org.solyton.solawi.bid.application.ui.page.auction.action.importBidders
import org.solyton.solawi.bid.module.bid.component.modal.showImportBiddersModal
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.reader.existRounds
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.data.rounds
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun ImportBiddersButton(
    storage: Storage<Application>,
    newBidders: Storage<List<NewBidder>>,
    auction: Lens<Application, Auction>,
    texts : Reader<Unit, Lang.Block>
) {
    val isDisabled = (storage * auction * rounds * existRounds).emit() ||
        (storage * auction * auctionAccepted).emit()

    StdButton(
        texts * text,
        storage * deviceData * mediaType.get,
        isDisabled
    ) {
        (storage * modals).showImportBiddersModal(
            storage * auction,
            texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.importBiddersDialog"),
            setBidders = { newBidders.write(it) },
            device = storage * deviceData * mediaType.get,
            cancel = {},
            update = {
                CoroutineScope(Job()).launch {
                    (storage * actions).read().emit(importBidders(newBidders.read(), auction))
                }
            }
        )
    }
}
