package org.solyton.solawil.bid.module.bid.data.api

import org.solyton.solawi.bid.module.values.UserId
import java.util.*

fun UserId.toUUID(): UUID = UUID.fromString(this.value)
fun UUID.toUserId() = UserId(toString())
