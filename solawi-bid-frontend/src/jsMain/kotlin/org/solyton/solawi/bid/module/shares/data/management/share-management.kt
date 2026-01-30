package org.solyton.solawi.bid.module.shares.data.management

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.types.ShareType

@Lensify
data class ShareManagement(
    @ReadWrite val shareSubscriptions: List<ShareSubscription> = emptyList(),
    @ReadWrite val shareOffers: List<ShareOffer> = emptyList(),
    @ReadWrite val shareTypes: List<ShareType> = emptyList()
)
