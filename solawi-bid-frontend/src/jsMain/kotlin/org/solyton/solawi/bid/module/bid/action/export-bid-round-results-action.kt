package org.solyton.solawi.bid.module.bid.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.bidround.BidResult
import org.solyton.solawi.bid.module.bid.data.bidround.BidRoundResults
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.api.ApiBidRoundResults
import org.solyton.solawi.bid.module.bid.data.api.ExportBidRound
import org.solyton.solawi.bid.module.bid.data.bidround.rawResults

@Markup
fun exportBidRoundResults(auctionId: String, round: Lens<BidApplication,Round>) = Action<BidApplication, ExportBidRound, ApiBidRoundResults >(
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
