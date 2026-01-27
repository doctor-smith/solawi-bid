package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.banking.data.api.FiscalYear

typealias ApiShareOffer = ShareOffer
typealias ApiShareOffers = ShareOffers

@Serializable
data class ShareOffer(
    val id: String,
    val shareType: ShareType,
    val fiscalYear: FiscalYear,
    val price: Double?,
    val pricingType: PricingType,
    val ahcAuthorizationRequired: Boolean
)

@Serializable
data class ShareOffers(
    val all: List<ShareOffer>
)

@Serializable
data class CreateShareOffer(
    val shareTypeId: String,
    val fiscalYearId: String,
    val price: Double?,
    val pricingType: PricingType,
    val ahcAuthorizationRequired: Boolean
)

@Serializable
data class UpdateShareOffer(
    val shareTypeId: String,
    val fiscalYearId: String,
    val price: Double?,
    val pricingType: PricingType,
    val ahcAuthorizationRequired: Boolean
)
