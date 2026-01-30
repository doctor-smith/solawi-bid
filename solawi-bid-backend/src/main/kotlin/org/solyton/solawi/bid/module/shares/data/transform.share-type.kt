package org.solyton.solawi.bid.module.shares.data

import org.solyton.solawi.bid.module.shares.data.api.ApiShareType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareTypes
import org.solyton.solawi.bid.module.shares.schema.ShareTypeEntity

fun List<ShareTypeEntity>.toApiType() : ApiShareTypes = ApiShareTypes(
    map { it.toApiType() }
)

fun ShareTypeEntity.toApiType(): ApiShareType = ApiShareType(
    id = id.value.toString(),
    name = name,
    description = description,
    providerId = providerId.toString(),
)
