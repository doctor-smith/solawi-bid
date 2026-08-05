package org.solyton.solawi.bid.module.user.data.internal

import java.util.*

data class Address(
    val addressId: UUID,
    val userProfileId: UUID? = null,
    val recipientName: String,
    val organizationName: String?,
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val stateOrProvince: String,
    val postalCode: String,
    val countryCode: String
)
