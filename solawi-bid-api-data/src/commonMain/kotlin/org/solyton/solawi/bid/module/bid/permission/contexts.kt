package org.solyton.solawi.bid.module.bid.permission

import org.evoleq.permission.combine
import org.evoleq.value.StringValueWithDescription

object AuctionContext  : StringValueWithDescription {
    override val value = Value.AUCTION
    override val description = ""

    object Management : StringValueWithDescription {
        override val value = combine(
            AuctionContext.value,
            Value.MANAGEMENT
        )
        override val description = ""
    }
}
