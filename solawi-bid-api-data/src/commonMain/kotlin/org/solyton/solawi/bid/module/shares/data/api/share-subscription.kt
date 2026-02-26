package org.solyton.solawi.bid.module.shares.data.api

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.values.isValidEmail
import org.solyton.solawi.bid.module.values.isValidUUID

typealias ApiShareSubscription = ShareSubscription
typealias ApiShareSubscriptions = ShareSubscriptions

@Serializable
data class ShareSubscriptions(
    val all: List<ShareSubscription>
)

@Serializable
data class ShareSubscription(
    val id: String,
    val providerId: String,
    val fiscalYearId: String,
    val shareOfferId: String,
    val userProfileId: String,
    val distributionPointId: String?,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
    val status: ShareStatus,
    val coSubscribers: List<String> = emptyList(),
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
) {
    init{
        require(isValidUUID(providerId)) {"Provider id is not a valid UUID: $providerId"}
        require(isValidUUID(shareOfferId)) {"Share offer id is not a valid UUID: $shareOfferId"}
        require(isValidUUID(userProfileId)) {"User profile id is not a valid UUID: $userProfileId"}
        distributionPointId?.let { require(isValidUUID(it)) {"Distribution point id is not a valid UUID: $it"} }
        fiscalYearId.let { require(isValidUUID(it)) {"Fiscal year id is not a valid UUID: $it"} }
        coSubscribers.forEach { require(isValidEmail(it)) {"Co subscriber id is not a valid UUID: $it"} }
    }
}

/**
 * Represents the parameters required to retrieve share subscriptions.
 *
 * This class extends the `Parameters` superclass and encapsulates the query parameters
 * necessary for operations involving share subscriptions.
 *
 * @property queryParams Specifies query parameters used in the request.  It requires the parameter: provider_id: UUID
 */
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
    val username: String,
    val distributionPointId: String?,
    val fiscalYearId: String,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
    val status: ShareStatus,
    val coSubscribers: List<String> = emptyList()
)
