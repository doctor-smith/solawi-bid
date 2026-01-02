package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable

typealias ApiShares = Shares
typealias ApiShare = Share

@Serializable
data class Shares(
    val all: List<Share>
)

@Serializable
data class Share(
    val id: String,
    val shareType: ShareType,
    val userProfileId: String,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val distributionPointId: String?,
    val fiscalYearId: String,
    val ahcAuthorized: Boolean?

)
