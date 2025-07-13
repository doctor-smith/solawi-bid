package org.solyton.solawi.bid.module.bid.component.effect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.device.data.mediaType
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.action.createRound
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.actions
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.bid.data.modals
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts

@Markup
@Suppress("FunctionName")
fun TriggerCreateNewRound(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>
) = CoroutineScope(Job()).launch{
    createNewRound(
        storage = storage,
        auction = auction
    )
}


@Markup
suspend fun createNewRound(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>
) = coroutineScope {
    CoroutineScope(Job()).launch {
        val actions = (storage * actions).read()
        try {
            actions.dispatch(createRound(auction))
        } catch (exception: Exception) {
            (storage * modals).showErrorModal(
                errorModalTexts(
                    exception.message ?: exception.cause?.message ?: "Cannot Emit action 'CreateRound' in update mode"
                ),
                storage * deviceData * mediaType.get
            )
        }
    }
}
