package org.solyton.solawi.bid.module.bid.component.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.module.bid.component.modal.showBidRoundEvaluationModal
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.Round
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.rounds
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun LaunchPresentationOfBidRoundEvaluationInModal(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round
) {
    LaunchedEffect(Unit) {
        launch{
            showBidRoundEvaluationModal(
                storage = storage,
                auction = auction,
                round = round
            )
        }
    }
}

@Markup
@Suppress("FunctionName")
fun TriggerPresentationOfBidRoundEvaluationInModal(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round
) = CoroutineScope(Job()) .launch{
    showBidRoundEvaluationModal(
        storage = storage,
        auction = auction,
        round = round
    )
}


@Markup
suspend fun showBidRoundEvaluationModal(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round
) = coroutineScope{
    (storage * modals).showBidRoundEvaluationModal(
        storage = storage,
        round = (auction * rounds * FirstBy { it.roundId == round.roundId }),// round.bidRoundEvaluationModal
        texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.round.bidRoundEvaluationModal"),
        device = (storage * deviceData * mediaType.get),
        cancel = if(round.state != RoundState.Frozen.toString()) {{
            //todo:decide start new round on button click?
            TriggerChangeRoundState(
                storage = storage,
                auction = auction,
                round = round
            )
            TriggerCreateNewRound(
                storage = storage,
                auction = auction
            )

        }} else null,
        update = if(round.state != RoundState.Frozen.toString()) {{
            TriggerChangeRoundState(
                storage = storage,
                auction = auction,
                round = round
            )
            //todo:dev implement mechanism to accept results of the bid round -> correct?
            TriggerAcceptRound(
                storage = storage,
                auction = auction,
                round = round
            )
        }} else {{}}
    )
}