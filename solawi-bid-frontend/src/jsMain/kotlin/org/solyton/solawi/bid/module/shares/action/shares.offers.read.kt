package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareOffers
import org.solyton.solawi.bid.module.shares.data.api.ReadShareOffers
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareOffers

const val READ_SHARE_OFFERS = "ReadShareOffers"

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
    name = READ_SHARE_OFFERS.suffixed(nameSuffix),
    reader = { ReadShareOffers(listOf("provider" to providerId)) },
    endPoint = ReadShareOffers::class,
    writer = shareOffers.set contraMap {sT -> sT.toDomainType()}
)
