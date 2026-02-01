package org.solyton.solawi.bid.module.shares.data.api

import kotlinx.serialization.Serializable

typealias ApiPricingType = PricingType

@Serializable
enum class PricingType {
    FIXED,
    FLEXIBLE
}
