package org.solyton.solawi.bid.application.permission

import org.evoleq.permission.EmptyContext
import org.evoleq.value.StringValueWithDescription
import org.solyton.solawi.bid.module.application.permission.ApplicationContext
import org.solyton.solawi.bid.module.bid.permission.AuctionContext
import org.solyton.solawi.bid.module.user.permission.OrganizationContext


data object Context {

    data object Empty : StringValueWithDescription by EmptyContext

    data object Application : StringValueWithDescription by ApplicationContext

    data object Organisation : StringValueWithDescription by OrganizationContext

    data object Auction : StringValueWithDescription by AuctionContext
}
