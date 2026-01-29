package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams

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
    val providerId: String,
    val shareOfferId: String,
    val userProfileId: String,
    val distributionPointId: String?,
    val fiscalYearId: String,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
    val coSubscribers: List<String> = emptyList()
)

@Serializable
data class ReadShareSubscriptions(override val queryParams: QueryParams): Parameters()

@Serializable
data class UpdateShareSubscription(
    val id: String,
    val providerId: String,
    val shareOfferId: String,
    val userProfileId: String,
    val distributionPointId: String?,
    val fiscalYearId: String,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
    val coSubscribers: List<String> = emptyList()
)

@Serializable
data class ImportShareSubscriptions(
    val override: Boolean = false,
    val providerId: String,
    val fiscalYearId: String,
    val shareSubscriptions: List<ImportShareSubscription>,
)

@Serializable
data class ImportShareSubscription(
    val shareOfferId: String,
    val userProfileId: String,
    val distributionPointId: String?,
    val fiscalYearId: String,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
    val status: ShareStatus,
    val coSubscribers: List<String> = emptyList()
)
