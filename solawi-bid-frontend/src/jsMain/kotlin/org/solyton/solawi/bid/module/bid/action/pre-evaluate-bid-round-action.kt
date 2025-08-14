package org.solyton.solawi.bid.module.bid.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiBidRoundPreEvaluation
import org.solyton.solawi.bid.module.bid.data.api.PreEvaluateBidRound
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.bidround.preEvaluation
import org.solyton.solawi.bid.module.bid.data.evaluation.BidRoundPreEvaluation
import org.solyton.solawi.bid.module.bid.data.toDomainType

@Markup
fun preEvaluateBidRound(auctionId: String, round: Lens<BidApplication, Round>): Action<BidApplication, PreEvaluateBidRound, ApiBidRoundPreEvaluation> = Action(
    name = "PreEvaluateBidRound",
    reader = round * Reader { r: Round -> PreEvaluateBidRound(auctionId, r.roundId) },
    endPoint = PreEvaluateBidRound::class,
    writer = (round * preEvaluation).set contraMap {
        evaluation: ApiBidRoundPreEvaluation -> BidRoundPreEvaluation(
            evaluation.auctionDetails.toDomainType(),
            evaluation.totalNumberOfShares
        )
    }
)
