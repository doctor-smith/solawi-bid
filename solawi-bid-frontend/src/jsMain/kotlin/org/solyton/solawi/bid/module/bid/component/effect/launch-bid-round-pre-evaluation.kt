package org.solyton.solawi.bid.module.bid.component.effect


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.device.data.mediaType
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.ui.page.auction.action.preEvaluateBidRound
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.actions
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.bid.data.modals
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts


@Markup
@Composable
@Suppress("FunctionName")
fun LaunchBidRoundPreEvaluation(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round
) {
    LaunchedEffect(Unit) {
        launch{
            bidRoundPreEvaluation(
                storage = storage,
                auction = auction,
                round = round
            )
        }
    }
}

@Markup
@Suppress("FunctionName")
fun TriggerBidRoundPreEvaluation(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round
) = CoroutineScope(Job()).launch{
    bidRoundPreEvaluation(
        storage = storage,
        auction = auction,
        round = round
    )
}

@Markup
suspend fun bidRoundPreEvaluation(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round
) = coroutineScope {
    val actions = (storage * actions).read()
    try {
        actions.dispatch(
            preEvaluateBidRound(
                (storage * auction).read().auctionId,
                auction * rounds * FirstBy { it.roundId == round.roundId })
        )
    } catch (exception: Exception) {
        (storage * modals).showErrorModal(
            errorModalTexts(
                exception.message ?: exception.cause?.message ?: "Cannot Emit action 'PreEvaluateBidRound'"
            ),
            storage * deviceData * mediaType.get
        )
    }
}
