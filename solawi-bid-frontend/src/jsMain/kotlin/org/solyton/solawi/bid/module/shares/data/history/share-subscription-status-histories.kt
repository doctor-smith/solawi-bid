package org.solyton.solawi.bid.module.shares.data.history

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite


@Lensify
data class ShareSubscriptionStatusHistories(
    @ReadWrite val all: List<ShareSubscriptionStatusHistory> = emptyList()
)
