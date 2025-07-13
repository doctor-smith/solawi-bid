package org.solyton.solawi.bid.module.bid.component.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.evoleq.device.data.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.application.ui.page.auction.action.exportBidRoundResults
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.auction.rounds
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts

@Markup
@Composable
@Suppress("FunctionName")
fun LaunchExportOfBidRoundResults(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round
) {
    LaunchedEffect(Unit){
        launch {
           exportBidRoundResults(
               storage = storage,
               auction = auction,
               round = round
           )
        }
    }
}
 @Markup
 @Suppress("FunctionName")
 fun TriggerExportOfBidRoundResults(
     storage: Storage<Application>,
     auction: Lens<Application, Auction>,
     round: Round
 ) = CoroutineScope(Job()).launch {
     exportBidRoundResults(
         storage = storage,
         auction = auction,
         round = round
     )
 }


@Markup
suspend fun CoroutineScope.exportBidRoundResults(
    storage: Storage<Application>,
    auction: Lens<Application, Auction>,
    round: Round
) = coroutineScope {
    val actions = (storage * actions).read()
    try {
        actions.emit( exportBidRoundResults(
            (storage * auction).read().auctionId,
            auction * rounds * FirstBy { it.roundId == round.roundId })
        )
    } catch(exception: Exception) {
        (storage * modals).showErrorModal(
            errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action 'ExportBidRound'"),
            storage * deviceData * mediaType.get
        )
    }
}
