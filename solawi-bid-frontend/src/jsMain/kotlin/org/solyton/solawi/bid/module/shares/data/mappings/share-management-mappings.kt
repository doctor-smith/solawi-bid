package org.solyton.solawi.bid.module.shares.data.mappings

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite

@Lensify
data class ShareManagementMappings(
    @ReadWrite val override: Boolean = false,
    @ReadWrite val providerId: String,
    @ReadWrite val fiscalYearId: String,
    @ReadWrite val shareOffers: Map<String, String>,
    @ReadWrite val distributionPoints: Map<String, String>
)
