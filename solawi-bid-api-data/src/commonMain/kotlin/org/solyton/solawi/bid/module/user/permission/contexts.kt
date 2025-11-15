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

object ApplicationContext: StringValueWithDescription {
    override val value = Value.APPLICATION
    override val description = ""

    object Organization : StringValueWithDescription {
        override val value = combine( Value.APPLICATION, Value.ORGANIZATION )
        override val description = ""
    }
}
