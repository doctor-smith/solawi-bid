package org.solyton.solawi.bid.module.shares.data.history

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId


@Lensify
data class ShareSubscriptionStatusHistory(
    @ReadWrite val shareSubscriptionId: ShareSubscriptionId,
    @ReadWrite val entries: List<ShareSubscriptionStatusHistoryEntry> = emptyList()
)
