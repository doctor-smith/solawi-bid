package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.language.text
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.module.bid.component.effect.TriggerCreateNewRound
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.auctionDetails
import org.solyton.solawi.bid.module.bid.data.reader.areNotConfigured
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.data.reader.biddersHaveNotBeenImported
import org.solyton.solawi.bid.module.bid.data.reader.existsRunning
import org.solyton.solawi.bid.module.bid.data.rounds
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.permissions.service.isNotGranted

@Markup
@Composable
@Suppress("FunctionName")
fun CreateNewRoundButton(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    texts : Reader<Unit, Lang.Block>,
    dataId: String
) {
    // New rounds can only be created when
    // 1. the auction is configured,
    // 2. the bidders have been imported and
    // 3. There are no open or running rounds
    // 4. auction has no accepted round
    val isDisabled = (storage * auction * rounds * existsRunning).emit() ||
        (storage * auction * auctionDetails * areNotConfigured).emit() ||
        (storage * auction * biddersHaveNotBeenImported).emit() ||
        (storage * auction * auctionAccepted).emit() ||
        (storage * userData.get).emit().isNotGranted(Right.Auction.manage)

    StdButton(
        texts * text,
        storage * deviceData * mediaType.get,
        isDisabled,
        dataId
    ) {
        TriggerCreateNewRound(
            storage = storage,
            auction = auction
        )
    }
}
