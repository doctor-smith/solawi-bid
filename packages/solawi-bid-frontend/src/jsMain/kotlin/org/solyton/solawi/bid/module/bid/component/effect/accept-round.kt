package org.solyton.solawi.bid.module.bid.component.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.application.ui.page.auction.action.acceptRound
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.Round
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts


@Markup
@Composable
@Suppress("FunctionName")
fun LaunchAcceptRound(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round
) {
    LaunchedEffect(Unit) {
        launch{
            acceptRound(
                storage = storage,
                auction = auction,
                round = round
            )
        }
    }
}

@Markup
@Suppress("FunctionName")
fun TriggerAcceptRound(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round
) = CoroutineScope(Job()).launch{
    acceptRound(
        storage = storage,
        auction = auction,
        round = round
    )
}

@Markup
suspend fun acceptRound(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round
) = coroutineScope {
    val actions = (storage * actions).read()
    try {
        actions.emit(
            acceptRound(
                auction,
                round.roundId
            )
        )
    } catch (exception: Exception) {
        (storage * modals).showErrorModal(
            texts = errorModalTexts(
                exception.message ?: exception.cause?.message ?: "Cannot Emit action 'AcceptRound'"
            ),
            device = storage * deviceData * mediaType.get,
        )
    }
}
