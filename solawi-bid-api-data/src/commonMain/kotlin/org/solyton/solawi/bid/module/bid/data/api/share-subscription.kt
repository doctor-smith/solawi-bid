package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

typealias ApiShareSubscription = ShareSubscription
typealias ApiShareSubscriptions = ShareSubscriptions

@Serializable
data class ShareSubscriptions(
    val all: List<ShareSubscription>
)

@Serializable
data class ShareSubscription(
    val id: String,
    val shareOffer: ShareOffer,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
    val distributionPointId: String?,
    val userProfileId: String,
    val status: ShareStatus,
    val statusUpdatedAt: LocalDateTime
)

@Serializable
data class CreateShareSubscription(
    val shareOfferId: String,
    val userProfileId: String,
    val distributionPointId: String?,
    val fiscalYearId: String,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
)

@Serializable
data class UpdateShareSubscription(
    val id: String,
    val shareOfferId: String,
    val userProfileId: String,
    val distributionPointId: String?,
    val fiscalYearId: String,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
)
