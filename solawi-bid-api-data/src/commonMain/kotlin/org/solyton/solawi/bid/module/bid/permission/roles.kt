package org.solyton.solawi.bid.module.bid.permission

import org.evoleq.value.StringValueWithDescription


object Bidder : StringValueWithDescription {
    override val value  = "BIDDER"
    override val description: String = "Participant in a bid-round, context: AUCTION"
}
