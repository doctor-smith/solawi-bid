package org.solyton.solawi.bid.module.user.permission

import org.evoleq.value.StringValueWithDescription

object OrganizationRight {
    object Organization {
        val create = object : StringValueWithDescription {
            override val value: String= "CREATE_ORGANIZATION"
            override val description: String = "Create organization in a context"
        }
        val read = object : StringValueWithDescription {
            override val value: String= "READ_ORGANIZATION"
            override val description: String = "Read organization in a context"
        }
        val update = object : StringValueWithDescription {
            override val value: String= "UPDATE_ORGANIZATION"
            override val description: String = "Update organization in a context"
        }
        val delete = object : StringValueWithDescription {
            override val value: String= "DELETE_ORGANIZATION"
            override val description: String = "Delete organization in a context"
        }
    }
}
