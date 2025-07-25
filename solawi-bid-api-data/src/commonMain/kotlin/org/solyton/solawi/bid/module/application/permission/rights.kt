package org.solyton.solawi.bid.module.application.permission

import org.evoleq.value.StringValueWithDescription

object AppRight {
    object Application {
        object Users {
            val manage = object : StringValueWithDescription {
                override val value: String = "MANAGE_USERS"
                override val description: String = "Manage Users"
            }
        }

        object Subscriptions {
            val readAvailableApplications = object : StringValueWithDescription {
                override val value: String = "READ_AVAILABLE_APPLICATIONS"
                override val description: String = "Read available application subscriptions"
            }
            val subscribeApplications = object : StringValueWithDescription {
                override val value: String = "SUBSCRIBE_APPLICATIONS"
                override val description: String = "available applications"
            }
            val unsubscribeApplications = object : StringValueWithDescription {
                override val value: String = "UNSUBSCRIBE_APPLICATIONS"
                override val description: String = "unsubscribe applications"
            }
        }
    }
}
