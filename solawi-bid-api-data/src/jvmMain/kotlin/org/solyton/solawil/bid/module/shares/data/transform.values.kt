package org.solyton.solawil.bid.module.shares.data

import org.solyton.solawi.bid.module.shares.data.values.ShareOfferId
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.data.values.ShareTypeId
import java.util.UUID

fun ShareSubscriptionId.toUUID(): UUID = UUID.fromString(value)

fun UUID.toShareSubscriptionId(): ShareSubscriptionId = ShareSubscriptionId(toString())


fun ShareTypeId.toUUID(): UUID = UUID.fromString(value)

fun UUID.toShareTypeId(): ShareTypeId = ShareTypeId(toString())


fun ShareOfferId.toUUID(): UUID = UUID.fromString(value)

fun UUID.toShareOfferId(): ShareOfferId = ShareOfferId(toString())
