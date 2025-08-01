package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.device.data.mediaType
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.component.effect.TriggerBidRoundEvaluation
import org.solyton.solawi.bid.module.bid.component.effect.TriggerPresentationOfBidRoundEvaluationInModal
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.control.button.StdButton

@Markup
@Composable
@Suppress("FunctionName")
fun EvaluateBidRoundButton(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round
) {
    val isDisabled = round.state !in listOf(RoundState.Evaluated.toString(), RoundState.Closed.toString(), RoundState.Frozen.toString())
    StdButton(
        // todo:i18n
        {"Evaluation"},
        storage * deviceData * mediaType.get,
        true //isDisabled todo:dev enable button and fix loading issues
    ) {
        if(round.bidRoundEvaluation.weightedBids.isEmpty()) {
            CoroutineScope(Job()).launch {
                TriggerBidRoundEvaluation(
                    storage,
                    auction,
                    round
                ).join()
                TriggerPresentationOfBidRoundEvaluationInModal(
                    storage = storage,
                    auction = auction,
                    round = round
                )
            }
        } else {
            TriggerPresentationOfBidRoundEvaluationInModal(
                storage = storage,
                auction = auction,
                round = round
            )
        }

    }
}
