package org.solyton.solawil.bid.module.bid.data.api

import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.values.UserProfileId
import java.util.*

fun UserId.toUUID(): UUID = UUID.fromString(this.value)
fun UUID.toUserId() = UserId(toString())

fun UserProfileId.toUUID(): UUID = UUID.fromString(this.value)
fun UUID.toUserProfileId() = UserProfileId(toString())


