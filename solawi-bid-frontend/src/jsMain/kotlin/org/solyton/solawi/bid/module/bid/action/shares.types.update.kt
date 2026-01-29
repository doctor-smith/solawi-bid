package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.bid.data.api.ApiShareType
import org.solyton.solawi.bid.module.bid.data.api.CreateShareType
import org.solyton.solawi.bid.module.bid.data.api.UpdateShareType
import org.solyton.solawi.bid.module.bid.data.shares.management.ShareManagement
import org.solyton.solawi.bid.module.bid.data.shares.management.shareTypes
import org.solyton.solawi.bid.module.bid.data.toDomainType

/**
 * Updates an existing share type with the provided details.
 *
 * @param shareTypeId The unique identifier of the share type to be updated.
 * @param providerId The unique identifier of the provider associated with the share type.
 * @param name The name of the share type.
 * @param key A unique key associated with the share type.
 * @param description A textual description providing additional information about the share type.
 * @param nameSuffix An optional suffix to append to the action name (default is an empty string).
 * @return An Action that processes the input to update the share type and updates the application storage accordingly.
 */
fun updateShareType(
    shareTypeId: String,
    providerId: String,
    name: String,
    key: String,
    description: String,
    nameSuffix: String = ""
): Action<ShareManagement, UpdateShareType, ApiShareType> = Action(
    name = "UpdateShareType$nameSuffix",
    reader = { _ -> UpdateShareType(
        id = shareTypeId,
        providerId = providerId,
        name = name,
        key = key,
        description = description
    ) },
    endPoint = UpdateShareType::class,
    writer = shareTypes.update {
        p, q -> p.shareTypeId == q.shareTypeId
    } contraMap {
        sT: ApiShareType -> sT.toDomainType()
    }
)
