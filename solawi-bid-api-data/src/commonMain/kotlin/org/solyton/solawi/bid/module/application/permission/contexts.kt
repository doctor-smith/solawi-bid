package org.solyton.solawi.bid.module.application.permission

import org.evoleq.permission.combine
import org.evoleq.value.StringValueWithDescription


object ApplicationContext: StringValueWithDescription {
    override val value = Value.APPLICATION
    override val description = ""

    object Organization : StringValueWithDescription {
        override val value = combine( Value.APPLICATION, Value.ORGANIZATION )
        override val description = ""
    }
}
