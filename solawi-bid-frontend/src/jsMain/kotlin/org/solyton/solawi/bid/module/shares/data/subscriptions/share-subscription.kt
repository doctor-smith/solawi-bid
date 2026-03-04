package org.solyton.solawi.bid.module.shares.data.subscriptions

import kotlinx.datetime.LocalDateTime
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.values.isValidEmail
import org.solyton.solawi.bid.module.values.isValidUUID

@Lensify
data class ShareSubscription (
    @ReadOnly val shareSubscriptionId: String,
    @ReadWrite val providerId: String,
    @ReadWrite val shareOfferId: String,
    @ReadWrite val userProfileId: String,
    @ReadWrite val distributionPointId: String?,
    @ReadWrite val fiscalYearId: String,
    @ReadWrite val numberOfShares: Int = 1,
    @ReadWrite val pricePerShare: Double? = null,
    @ReadWrite val ahcAuthorized: Boolean? = false,
    @ReadWrite val status: ShareStatus,
    @ReadWrite val coSubscribers: List<String> = emptyList(),
    @ReadOnly val statusUpdatedAt: LocalDateTime? = null,
) {
    init {
        require(isValidUUID(providerId)) {"Provider id is not a valid UUID: $providerId"}
        require(isValidUUID(shareOfferId)) {"Share offer id is not a valid UUID: $shareOfferId"}
        require(isValidUUID(userProfileId)) {"User profile id is not a valid UUID: $userProfileId"}
        distributionPointId?.let { require(isValidUUID(it)) {"Distribution point id is not a valid UUID: $it"} }
        fiscalYearId.let { require(isValidUUID(it)) {"Fiscal year id is not a valid UUID: $it"} }
        coSubscribers.forEach { require(isValidEmail(it)) {"Co subscriber is not a valid Email: $it"} }
    }
}

