package org.solyton.solawi.bid.module.user.permission

import org.evoleq.permission.combine
import org.evoleq.value.StringValueWithDescription

object OrganizationContext : StringValueWithDescription {
    override val value = Value.ORGANIZATION
    override val description = ""

    object Management : StringValueWithDescription {
        override val value = combine(OrganizationContext.value, Value.MANAGEMENT)
        override val description = ""
    }
}
