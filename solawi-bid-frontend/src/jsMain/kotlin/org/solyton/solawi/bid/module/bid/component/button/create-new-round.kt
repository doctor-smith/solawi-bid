package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.text
import org.evoleq.language.tooltip
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Color
import org.solyton.solawi.bid.module.bid.component.effect.TriggerCreateNewRound
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.auctionDetails
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.bid.data.reader.areNotConfigured
import org.solyton.solawi.bid.module.bid.data.reader.auctionAccepted
import org.solyton.solawi.bid.module.bid.data.reader.biddersHaveNotBeenImported
import org.solyton.solawi.bid.module.bid.data.reader.existsRunning
import org.solyton.solawi.bid.module.bid.data.user
import org.solyton.solawi.bid.module.bid.permission.BidRight
import org.solyton.solawi.bid.module.bid.service.isNotGranted
import org.solyton.solawi.bid.module.control.button.PlayButton
import org.solyton.solawi.bid.module.control.button.PlayButtonWithText
import org.solyton.solawi.bid.module.style.data.Side
import org.w3c.dom.Text

@Markup
@Composable
@Suppress("FunctionName")
fun CreateNewRoundButton(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    texts : Reader<Unit, Lang.Block>,
    dataId: String,
    showText: Boolean = false
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
        (storage * user.get).emit().isNotGranted(BidRight.Auction.manage)

    when(showText) {
        true -> PlayButtonWithText(
            Color.black,
            Color.transparent,
            texts * text,
            texts * tooltip,
            Side.Right,
            storage * deviceData * mediaType.get,
            isDisabled,
            dataId
        ) {
            TriggerCreateNewRound(
                storage = storage,
                auction = auction
            )
        }
        false -> PlayButton(
            Color.black,
            Color.transparent,
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
}
