package org.solyton.solawi.bid.module.shares.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.values.UserId

typealias ApiShareSubscriptionStatusHistory = ShareSubscriptionStatusHistory
typealias ApiShareSubscriptionStatusHistories = ShareSubscriptionStatusHistories
typealias ApiShareSubscriptionStatusHistoryEntry = ShareSubscriptionStatusHistoryEntry

@Serializable
data class ShareSubscriptionStatusHistories(
    val all: List<ShareSubscriptionStatusHistory>
)

@Serializable
data class ShareSubscriptionStatusHistory(
    val shareSubscriptionId: ShareSubscriptionId,
    val entries: List<ShareSubscriptionStatusHistoryEntry>
)

@Serializable
data class ShareSubscriptionStatusHistoryEntry(
    val rollingOverFromShareSubscriptionId: ShareSubscriptionId? = null,
    val fromStatus: ShareStatus?,
    val toStatus: ShareStatus,
    val reason: ChangeReason,
    val changedBy: ChangedBy,
    val comment: String?,
    val humanModifierId: UserId? = null
)

/**
 * Represents the parameters required to retrieve share subscriptions.
 *
 * This class extends the `Parameters` superclass and encapsulates the query parameters
 * necessary for operations involving share subscriptions.
 *
 * @property queryParams Specifies query parameters used in the request.  It requires the parameter: share_subscription_id: UUID
 */
@Serializable
data class ReadShareSubscriptionStatusHistory(override val queryParams: QueryParams): Parameters()
