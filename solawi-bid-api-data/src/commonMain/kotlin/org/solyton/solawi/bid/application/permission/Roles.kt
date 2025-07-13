package org.solyton.solawi.bid.application.permission

import org.evoleq.permission.Manager
import org.evoleq.permission.Owner
import org.evoleq.permission.User
import org.solyton.solawi.bid.module.bid.permission.Bidder

object Role {
    val owner = Owner
    val manager = Manager
    val user = User
    val bidder = Bidder
}
