package org.solyton.solawi.bid.module.user.data.api.userprofile

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String,
    val recipientName: String,
    val organizationName: String?,
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val stateOrProvince: String,
    val postalCode: String,
    val countryCode: String
)
