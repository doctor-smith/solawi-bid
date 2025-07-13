package org.evoleq.permission

import org.evoleq.value.StringValueWithDescription

object Role {
    val owner = Owner
    val manager = Manager
    val user = User
}

object Owner : StringValueWithDescription {
    override val value  = "OWNER"
    override val description: String = "Owner owns a resource or context"
}

object Manager : StringValueWithDescription {
    override val value  = "MANAGER"
    override val description: String = "Manages a resource or context"
}

object User : StringValueWithDescription {
    override val value  = "USER"
    override val description: String = "User of the application, context: APPLICATION"
}
