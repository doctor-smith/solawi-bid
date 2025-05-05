package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.BidResult
import org.solyton.solawi.bid.module.bid.data.BidRoundResults
import org.solyton.solawi.bid.module.bid.data.Round
import org.solyton.solawi.bid.module.bid.data.api.ApiBidRoundResults
import org.solyton.solawi.bid.module.bid.data.api.ExportBidRound
import org.solyton.solawi.bid.module.bid.data.rawResults

@Markup
fun exportBidRoundResults(auctionId: String, round: Lens<Application,Round>) = Action<Application, ExportBidRound, ApiBidRoundResults >(
    name = "ExportBidRound",
    reader = round * Reader { r: Round -> ExportBidRound(r.roundId, auctionId ) },
    endPoint = ExportBidRound::class,
    writer = (round * rawResults).set contraMap {
        // todo move to separate transform function
        apiBidRoundResults: ApiBidRoundResults ->  BidRoundResults(
            bidRoundResults = apiBidRoundResults.results.map{
                BidResult(
                    it.username,
                    it.numberOfShares,
                    it.amount,
                    it.hasPlacedBid
                )
            },
            startDownloadOfBidRoundResults = true
        )
    }
)
