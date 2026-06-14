package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable

typealias ApiUserStatus = UserStatus

@Serializable
enum class UserStatus {
    REGISTERED, // waiting for email verification
    PENDING,    // User created, invite not sent yet, login not allowed
    INVITED, // Invite sent, password can be set, login still blocked
    ACTIVE,     // Password set, login allowed
    DISABLED    // Account disabled manually or automatically, login blocked
}
