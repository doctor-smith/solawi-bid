package org.solyton.solawi.bid.module.bid.data.shares.management

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.bid.data.shares.offers.ShareOffer
import org.solyton.solawi.bid.module.bid.data.shares.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.bid.data.shares.types.ShareType

@Lensify
data class ShareManagement(
    @ReadWrite val shareSubscriptions: List<ShareSubscription> = emptyList(),
    @ReadWrite val shareOffers: List<ShareOffer> = emptyList(),
    @ReadWrite val shareTypes: List<ShareType> = emptyList()
)
