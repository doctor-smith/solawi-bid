package org.solyton.solawi.bid.module.bid.data

import org.solyton.solawi.bid.module.bid.data.api.ApiShareType
import org.solyton.solawi.bid.module.bid.data.api.ApiShareTypes
import org.solyton.solawi.bid.module.bid.schema.ShareTypeEntity

fun List<ShareTypeEntity>.toApiType() : ApiShareTypes = ApiShareTypes(
    map { it.toApiType() }
)

fun ShareTypeEntity.toApiType(): ApiShareType = ApiShareType(
    id = id.value.toString(),
    name = name,
    description = description,
    providerId = providerId.toString(),
)
