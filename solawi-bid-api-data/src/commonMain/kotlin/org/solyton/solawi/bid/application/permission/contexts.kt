package org.solyton.solawi.bid.application.permission

import org.evoleq.value.StringValueWithDescription


data object Context {

    data object Empty : StringValueWithDescription {
        override val value = Value.EMPTY
        override val description = ""
    }

    data object Application : StringValueWithDescription {
        override val value = Value.APPLICATION
        override val description = ""

        object Organization : StringValueWithDescription {
            override val value = combine( Value.APPLICATION, Value.ORGANIZATION )
            override val description = ""
        }
    }

    object Organization : StringValueWithDescription {
        override val value = Value.ORGANIZATION
        override val description = ""

        object Management : StringValueWithDescription {
            override val value = combine( Organization.value, Value.MANAGEMENT)
            override val description = ""
        }
    }

    object Auction  : StringValueWithDescription {
        override val value = Value.AUCTION
        override val description = ""

        object Management : StringValueWithDescription {
            override val value = combine(Auction.value, Value.MANAGEMENT)
            override val description = ""
        }
    }
}

internal fun combine(vararg contexts: String): String = contexts.joinToString("/") { it }


object Value {
    const val EMPTY = "EMPTY"
    const val ORGANIZATION = "ORGANIZATION"
    const val APPLICATION = "APPLICATION"
    const val MANAGEMENT = "MANAGEMENT"
    const val AUCTION = "AUCTION"
}
