package org.solyton.solawi.bid.module.user.service

import org.solyton.solawi.bid.module.user.data.User
import org.evoleq.value.StringValueWithDescription

private val owner = "owner@solyton.org"
private val fs = "schmidt@alpha-structure.com"

fun User.isOwner() = username == owner || username == fs

fun User.isGranted(right: StringValueWithDescription): Boolean = when(right.value) {
    "MANAGE_BID_ROUND" -> isOwner()
    "MANAGE_AUCTION" -> isOwner()
    "MANAGE_USERS" -> isOwner()
    else -> true
}

fun User.isNotGranted(right: StringValueWithDescription) = !isGranted(right)
