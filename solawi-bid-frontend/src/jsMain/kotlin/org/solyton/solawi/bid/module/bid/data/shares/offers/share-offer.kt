package org.solyton.solawi.bid.module.bid.data.shares.offers

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.bid.data.api.PricingType
import org.solyton.solawi.bid.module.bid.data.shares.types.ShareType

@Lensify
data class ShareOffer (
    @ReadOnly val shareOfferId: String,
    @ReadWrite val shareType: ShareType,
    @ReadWrite val fiscalYear: FiscalYear,
    @ReadWrite val price: Double?,
    @ReadWrite val pricingType: PricingType,
    @ReadWrite val ahcAuthorizationRequired: Boolean
)
