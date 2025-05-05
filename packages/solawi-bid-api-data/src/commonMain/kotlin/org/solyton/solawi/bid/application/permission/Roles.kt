package org.solyton.solawi.bid.application.permission

import org.solyton.solawi.bid.shared.ValueWithDescription

object Role {
    val owner = Owner
    val manager = Manager
    val user = User
    val bidder = Bidder
}

object Owner : ValueWithDescription {
    override val value  = "OWNER"
    override val description: String = "Owner owns a resource or context"
}

object Manager : ValueWithDescription {
    override val value  = "MANAGER"
    override val description: String = "Manages a resource or context"
}

object User : ValueWithDescription {
    override val value  = "USER"
    override val description: String = "User of the application, context: APPLICATION"
}

object Bidder : ValueWithDescription {
    override val value  = "BIDDER"
    override val description: String = "Participant in a bid-round, context: AUCTION"
}

