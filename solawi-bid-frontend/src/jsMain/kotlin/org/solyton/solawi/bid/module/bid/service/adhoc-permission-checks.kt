package org.solyton.solawi.bid.module.bid.service


import org.evoleq.value.StringValueWithDescription
import org.solyton.solawi.bid.module.bid.data.biduser.User

private const val OWNER = "owner@solyton.org"
private const val FS = "schmidt@alpha-structure.com"

fun User.isOwner() = username == OWNER || username == FS

fun User.isGranted(right: StringValueWithDescription): Boolean = when(right.value) {
    "MANAGE_BID_ROUND" -> isOwner()
    "MANAGE_AUCTION" -> isOwner()
    "MANAGE_USERS" -> isOwner()
    else -> true
}

fun User.isNotGranted(right: StringValueWithDescription) = !isGranted(right)
