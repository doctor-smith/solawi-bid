package org.solyton.solawi.bid.module.shares.data.mappings

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.types.ShareType

@Lensify
data class ShareManagementMappings(
    @ReadWrite val override: Boolean = false,
    @ReadWrite val providerId: String,
    @ReadWrite val fiscalYears: List<FiscalYear>,
    @ReadWrite val shareOffers: List<ShareOffer>,
    @ReadWrite val distributionPoints: Map<String, String>
)
