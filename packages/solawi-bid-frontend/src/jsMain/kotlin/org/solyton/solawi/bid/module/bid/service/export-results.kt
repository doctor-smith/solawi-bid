package org.solyton.solawi.bid.module.bid.service

import org.solyton.solawi.bid.module.bid.data.BidRoundResults

fun BidRoundResults.toCsvContent(): String = """
    |Email;Anteile;Gebot;HatGeboten
    |${bidRoundResults.joinToString("\n") { with(it){"$resultingUsername;$resultingNumberOfShares;$resultingBidAmount;$resultingHasPlacedBid"} }}
""".trimMargin()
