package org.solyton.solawi.bid.module.bid.permission

import org.evoleq.value.StringValueWithDescription

object BidRight {

    object BidRound {
        val manage = object : StringValueWithDescription {
            override val value: String= "MANAGE_BID_ROUND"
            override val description: String = "Manage Bid round"
        }

    }
    object Auction {
        val manage = object : StringValueWithDescription {
            override val value: String= "MANAGE_AUCTION"
            override val description: String = "Manage Auction"
        }
    }
}
