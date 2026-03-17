package org.solyton.solawi.bid.module.bid.component.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.evoleq.compose.Markup
import org.evoleq.compose.download.downloadCsv
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.bidround.rawResults
import org.solyton.solawi.bid.module.bid.data.bidround.startDownloadOfBidRoundResults
import org.solyton.solawi.bid.module.bid.service.toCsvContent
import kotlin.js.Date

@Markup
@Composable
@Suppress("FunctionName", "UNUSED_PARAMETER" /* todo:dev remove after finishing i18n */)
fun LaunchDownloadOfBidRoundResults(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round,
    texts: Source<Lang.Block>
) {
    if(round.rawResults.startDownloadOfBidRoundResults) {
        LaunchedEffect(Unit) {
            // todo:i18n
            val fileName = "results_${Date.now()}.csv"
            val csvContent = round.rawResults.toCsvContent()
            downloadCsv(csvContent, fileName)
        }
        val startDownload = (storage * auction * rounds * FirstBy { it.roundId == round.roundId }) * rawResults * startDownloadOfBidRoundResults
        startDownload.write(false)
    }
}
