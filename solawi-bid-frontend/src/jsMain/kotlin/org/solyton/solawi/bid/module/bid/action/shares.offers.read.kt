package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.bid.data.api.ApiShareOffers
import org.solyton.solawi.bid.module.bid.data.api.ReadShareOffers
import org.solyton.solawi.bid.module.bid.data.shares.management.ShareManagement
import org.solyton.solawi.bid.module.bid.data.shares.management.shareOffers
import org.solyton.solawi.bid.module.bid.data.toDomainType

/**
 * Reads the share offers associated with a specific provider.
 *
 * @param providerId The unique identifier of the provider for which the share offers should be read.
 * @param nameSuffix An optional suffix appended to the action's name.
 * @return An `Action` instance that reads the share offers associated with the given providerId, defining the input, endpoint, and writer for the operation.
 */
fun readShareOffers(
    providerId: String,
    nameSuffix: String = ""
) : Action<ShareManagement, ReadShareOffers, ApiShareOffers> = Action(
    name = "ReadShareOffers$nameSuffix",
    reader = { ReadShareOffers(listOf("provider_id" to providerId)) },
    endPoint = ReadShareOffers::class,
    writer = shareOffers.set contraMap {sT -> sT.toDomainType()}
)
