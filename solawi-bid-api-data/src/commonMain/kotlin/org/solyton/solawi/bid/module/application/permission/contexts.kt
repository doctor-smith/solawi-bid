package org.solyton.solawi.bid.module.application.permission

import org.evoleq.permission.combine
import org.evoleq.value.StringValueWithDescription


import org.evoleq.permission.EmptyContext
import org.solyton.solawi.bid.module.bid.permission.AuctionContext
import org.solyton.solawi.bid.module.user.permission.OrganizationContext


data object Context {

    data object Empty : StringValueWithDescription by EmptyContext

    data object Application : StringValueWithDescription by ApplicationContext

    data object Organisation : StringValueWithDescription by OrganizationContext

    data object Auction : StringValueWithDescription by AuctionContext
}

object ApplicationContext: StringValueWithDescription {
    override val value = Value.APPLICATION
    override val description = ""

    object Organization : StringValueWithDescription {
        override val value = combine( Value.APPLICATION, Value.ORGANIZATION )
        override val description = ""
    }
}
