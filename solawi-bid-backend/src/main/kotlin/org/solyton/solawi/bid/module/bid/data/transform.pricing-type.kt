package org.solyton.solawi.bid.module.bid.data

import org.solyton.solawi.bid.module.bid.data.api.ApiPricingType
import org.solyton.solawi.bid.module.bid.schema.PricingType

fun ApiPricingType.toDomainType(): PricingType = when(this) {
    ApiPricingType.FIXED -> PricingType.FIXED
    ApiPricingType.FLEXIBLE -> PricingType.FLEXIBLE
}

fun PricingType.toApiType(): ApiPricingType = when(this) {
    PricingType.FIXED -> ApiPricingType.FIXED
    PricingType.FLEXIBLE -> ApiPricingType.FLEXIBLE
}
