package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.bid.component.effect.TriggerExportOfBidRoundResults
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.control.button.FileExportButton
import org.solyton.solawi.bid.module.style.button.buttonStyle

@Markup
@Composable
@Suppress("FunctionName")
fun ExportBidRoundResultsButton_Dep(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round,
    texts: Source<Lang.Block>
) {

    // todo:refactor:extract
    Button(attrs= {
        style {
            width(100.px)
            buttonStyle((storage * deviceData * mediaType).read())()
        }
        onClick {
            TriggerExportOfBidRoundResults(
                storage = storage,
                auction = auction,
                round = round
            )
        }
    }) {
        Text((texts * title).emit())
    }
}



@Markup
@Composable
@Suppress("FunctionName")
fun ExportBidRoundResultsButton(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round,
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    isDisabled: Boolean
) {
    FileExportButton(
        color = Color.black,
        bgColor = Color.transparent,
        texts = texts * title,
        deviceType = device,
        isDisabled = isDisabled,
        dataId = "auction:current-round:file-export-button",
    ) {
        TriggerExportOfBidRoundResults(
            storage = storage,
            auction = auction,
            round = round
        )
    }
}
