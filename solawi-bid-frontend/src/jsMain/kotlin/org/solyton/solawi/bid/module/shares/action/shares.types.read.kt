package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareTypes
import org.solyton.solawi.bid.module.shares.data.api.ReadShareTypes
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareTypes

/**
 * Reads the share types associated with a specific provider.
 *
 * @param providerId The unique identifier of the provider for which the share types should be read.
 * @param nameSuffix An optional suffix appended to the action's name.
 * @return An `Action` instance that reads the share types associated with the given providerId, defining the input, endpoint, and writer for the operation.
 */
fun readShareTypes(
    providerId: String,
    nameSuffix: String = ""
) : Action<ShareManagement, ReadShareTypes, ApiShareTypes> = Action(
    name = "ReadShareTypes$nameSuffix",
    reader = { ReadShareTypes(listOf("provider_id" to providerId)) },
    endPoint = ReadShareTypes::class,
    writer = shareTypes.set contraMap {sT -> sT.toDomainType()}
)
