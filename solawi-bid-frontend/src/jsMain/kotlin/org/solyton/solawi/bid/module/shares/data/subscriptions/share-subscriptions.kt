package org.solyton.solawi.bid.module.shares.data.subscriptions

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite


@Lensify
data class ShareSubscriptions(
    @ReadWrite val all: List<ShareSubscription>
)
