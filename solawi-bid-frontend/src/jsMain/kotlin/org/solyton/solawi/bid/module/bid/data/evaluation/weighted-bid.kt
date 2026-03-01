package org.solyton.solawi.bid.module.bid.data.evaluation

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly

@Lensify
data class WeightedBid(
    @ReadOnly val weight: Int,
    @ReadOnly val bid: Double,
    @ReadOnly val hasPlacedBid: Boolean
)
