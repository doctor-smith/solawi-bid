package org.solyton.solawi.bid.module.distribution.data.distributionpoint

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.user.data.address.Address

@Lensify
data class DistributionPoint(
    @ReadOnly val distributionPointId: String,
    @ReadWrite val name: String,
    @ReadWrite val address: Address?,
    @ReadWrite val organizationId: String
)
