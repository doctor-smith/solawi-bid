package org.solyton.solawi.bid.module.shares.data.subscriptions

import kotlinx.datetime.LocalDateTime
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus

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
)
