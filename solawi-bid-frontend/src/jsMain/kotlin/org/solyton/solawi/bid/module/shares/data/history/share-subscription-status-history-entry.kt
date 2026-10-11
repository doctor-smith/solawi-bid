package org.solyton.solawi.bid.module.shares.data.history

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.values.UserId


@Lensify
data class ShareSubscriptionStatusHistoryEntry(
    @ReadWrite val rollingOverFromShareSubscriptionId: ShareSubscriptionId? = null,
    @ReadWrite val fromStatus: ShareStatus?,
    @ReadWrite val toStatus: ShareStatus,
    @ReadWrite val reason: ChangeReason,
    @ReadWrite val changedBy: ChangedBy,
    @ReadWrite val comment: String?,
    @ReadWrite val humanModifierId: UserId? = null
)
