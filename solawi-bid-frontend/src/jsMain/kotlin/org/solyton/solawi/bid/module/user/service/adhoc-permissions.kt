package org.solyton.solawi.bid.module.user.service

import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.module.user.data.User
import org.evoleq.value.StringValueWithDescription




private val owner = "owner@solyton.org"
private val fs = "schmidt@alpha-structure.com"

fun User.isOwner() = username == owner || username == fs

fun User.isGranted(right: StringValueWithDescription): Boolean = when(right) {
    Right.BidRound.manage -> isOwner()
    Right.Auction.manage -> isOwner()
    Right.Application.Users.manage -> isOwner()
    else -> true
}

fun User.isNotGranted(right: StringValueWithDescription) = !isGranted(right)
