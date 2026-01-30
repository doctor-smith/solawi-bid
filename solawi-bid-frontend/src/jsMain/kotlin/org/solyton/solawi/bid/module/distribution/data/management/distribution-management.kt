package org.solyton.solawi.bid.module.distribution.data.management

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint

@Lensify
data class DistributionManagement(
    @ReadWrite val distributionPoints: List<DistributionPoint>
)
