package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareType
import org.solyton.solawi.bid.module.shares.data.api.CreateShareType
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareTypes


/**
 * Creates a new share type associated with a specific provider.
 *
 * @param providerId The unique identifier of the provider associated with the share type.
 * @param name The name of the share type.
 * @param key A unique key used to identify the share type.
 * @param description A description of the share type providing additional details.
 * @param nameSuffix An optional suffix appended to the action's name.
 * @return An `Action` instance responsible for creating the share type, involving the input data, endpoint, and writer logic for storage.
 */
fun createShareType(
    providerId: String,
    name: String,
    key: String,
    description: String,
    nameSuffix: String = ""
): Action<ShareManagement, CreateShareType, ApiShareType> = Action(
    name = "CreateShareType$nameSuffix",
    reader = { _ -> CreateShareType(
        providerId = providerId,
        name = name,
        key = key,
        description = description
    ) },
    endPoint = CreateShareType::class,
    writer = shareTypes.add() contraMap { sT: ApiShareType -> sT.toDomainType()}
)
