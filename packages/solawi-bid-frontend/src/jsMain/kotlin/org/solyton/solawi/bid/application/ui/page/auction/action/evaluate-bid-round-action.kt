package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.Round
import org.solyton.solawi.bid.module.bid.data.api.ApiBidRoundEvaluation
import org.solyton.solawi.bid.module.bid.data.api.EvaluateBidRound
import org.solyton.solawi.bid.module.bid.data.bidRoundEvaluation
import org.solyton.solawi.bid.module.bid.data.evaluation.BidRoundEvaluation
import org.solyton.solawi.bid.module.bid.data.evaluation.WeightedBid
import org.solyton.solawi.bid.module.bid.data.toDomainType

@Markup
fun evaluateBidRound(auctionId: String, round: Lens<Application, Round>): Action<Application, EvaluateBidRound, ApiBidRoundEvaluation> = Action(
    name = "EvaluateBidRound",
    reader = round * Reader { r:Round -> EvaluateBidRound(auctionId, r.roundId)},
    endPoint = EvaluateBidRound::class,
    writer = (round * bidRoundEvaluation).set contraMap {
        apiBidRoundEvaluation: ApiBidRoundEvaluation ->
        BidRoundEvaluation(
            auctionDetails = apiBidRoundEvaluation.auctionDetails.toDomainType(),
            totalSumOfWeightedBids = apiBidRoundEvaluation.totalSumOfWeightedBids,
            totalNumberOfShares = apiBidRoundEvaluation.totalNumberOfShares,
            weightedBids = apiBidRoundEvaluation.weightedBids.map {
                WeightedBid(
                    it.weight,
                    it.bid
                )
            }
        )
    }
)