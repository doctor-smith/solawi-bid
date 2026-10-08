package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.shares.data.api.ApiShareOffers
import org.solyton.solawi.bid.module.shares.data.api.ReadPersonalShareOffers
import org.solyton.solawi.bid.module.shares.data.api.ReadShareOffers
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.personalShareOffers
import org.solyton.solawi.bid.module.shares.data.management.shareOffers
import org.solyton.solawi.bid.module.shares.data.toDomainType

const val READ_SHARE_OFFERS = "READ_SHARE_OFFERS"

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



const val READ_PERSONAL_SHARE_OFFERS = "READ_PERSONAL_SHARE_OFFERS"

/**
 * Reads personal share offers. This includes all share-offers of all organizations the current user is a member of
 *
 * @param nameSuffix An optional suffix appended to the action's name, used to filter share offers for a specific member or group.
 * @return An `Action` instance that reads the personal share offers, defining the input parameters, target endpoint, and writer logic for processing the fetched data.
 */
fun readPersonalShareOffers(
    nameSuffix: String = ""
) : Action<ShareManagement, ReadPersonalShareOffers, ApiShareOffers> = Action(
    name = READ_PERSONAL_SHARE_OFFERS.suffixed(nameSuffix),
    reader = { ReadPersonalShareOffers(listOf("member" to nameSuffix)) },
    endPoint = ReadPersonalShareOffers::class,
    writer = personalShareOffers.set contraMap {sT -> sT.toDomainType()}
)
