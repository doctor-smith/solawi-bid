package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable

typealias ApiShareType = ShareType

@Serializable
data class ShareType(
    val id: String,
    val name: String,
    val description: String,
    val fixedPrice: Double?,
    val ahcAuthorizationRequired: Boolean
)
