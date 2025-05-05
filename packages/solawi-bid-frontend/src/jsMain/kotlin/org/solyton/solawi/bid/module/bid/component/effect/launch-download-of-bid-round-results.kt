package org.solyton.solawi.bid.module.bid.component.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.service.toCsvContent
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.Date

@Markup
@Composable
@Suppress("FunctionName")
fun LaunchDownloadOfBidRoundResults(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round,
    texts: Source<Lang.Block>
) {
    if(round.rawResults.startDownloadOfBidRoundResults) {
        LaunchedEffect(Unit) {
            val fileName = "results_${Date.now()}.csv"
            val csvContent = round.rawResults.toCsvContent()
            downloadCsv(csvContent, fileName)
        }
        val startDownload = (storage * auction * rounds * FirstBy { it.roundId == round.roundId }) * rawResults  * startDownloadOfBidRoundResults
        startDownload.write(false)
    }
}

fun downloadCsv(csvData: String, fileName: String = "bid-round-export.csv") {
    // Create a Blob from the CSV string
    val blob = Blob(arrayOf(csvData), BlobPropertyBag(type = "text/csv"))

    // Create an object URL
    val url = URL.createObjectURL(blob)

    // Create an anchor element and trigger download
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = fileName
    document.body?.appendChild(anchor)
    anchor.click()

    // Cleanup: remove the anchor and revoke the object URL
    document.body?.removeChild(anchor)
    URL.revokeObjectURL(url)
}
