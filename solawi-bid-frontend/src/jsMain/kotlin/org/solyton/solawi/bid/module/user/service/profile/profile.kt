package org.solyton.solawi.bid.module.user.service.profile

import org.solyton.solawi.bid.module.user.data.profile.UserProfile

fun UserProfile?.fullname(): String = when{
    this == null -> ""
    firstname.isBlank() -> lastname
    lastname.isBlank() -> firstname
    else -> "$firstname $lastname"
}

fun UserProfile?.firstAddress(): String {

    val userProfile = this
    return when {
        userProfile == null -> ""
        else -> userProfile.addresses.firstOrNull()?.let { addr ->
            buildString {
                append(addr.addressLine1)
                if (addr.addressLine2.isNotBlank()) {
                    append(", ")
                    append(addr.addressLine2)
                }
                append(", ")
                append(addr.postalCode)
                append(" ")
                append(addr.city)
            }
        } ?: ""
}}
