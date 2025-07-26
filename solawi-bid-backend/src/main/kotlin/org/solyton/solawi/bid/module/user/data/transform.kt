package org.solyton.solawi.bid.module.user.data

import org.solyton.solawi.bid.module.user.data.api.UserD
import org.solyton.solawi.bid.module.user.schema.User as UserEntity

fun UserEntity.toApiType(): UserD = UserD(
    id.value,
    username,
    password
)


