package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.ui.style.button.buttonStyle
import org.solyton.solawi.bid.module.bid.component.effect.TriggerExportOfBidRoundResults
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.Round

@Markup
@Composable
@Suppress("FunctionName")
fun ExportBidRoundResultsButton(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
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
