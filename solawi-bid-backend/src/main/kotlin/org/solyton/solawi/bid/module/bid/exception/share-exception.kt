package org.solyton.solawi.bid.module.bid.exception

import org.solyton.solawi.bid.module.bid.schema.PricingType

sealed class ShareException(override val message: String): Exception(message) {
    data class NoSuchShareType(val id: String) : ShareException("No such Share $id")
    data class NoSuchShareOffer(val id: String) : ShareException("No such ShareOffer $id")
    data class NoSuchShareSubscription(val id: String) : ShareException("No such ShareSubscription $id")

    data class CannotDeleteShareType(val id: String, val error: String) : ShareException(
        "Cannot delete ShareType $id; error = $error"
    )

    data class CannotDeleteShareTypesOfProvider(val id: String, val error: String) : ShareException(
        "Cannot delete ShareTypes of provider $id; error = $error"
    )

    data class CannotDeleteShareOffer(val id: String, val error: String) : ShareException(
        "Cannot delete ShareOffer $id; error = $error"
    )
    data class CannotDeleteShareSubscription(val id: String, val error: String) : ShareException(
        "Cannot delete ShareSubscription $id; error = $error"
    )
    data class DuplicateNameOfShareTypeAtProvider(val name: String, val providerId: String) : ShareException(
        "Duplicate name of ShareType $name at provider $providerId"
    )

    data class InvalidPricing(val price: Double?, val pricingType: PricingType) : ShareException(
        "Invalid Pricing: $price / $pricingType"
    )

    data class InvalidPricePerShare(val price: Double?, val pricingType: PricingType) : ShareException(
        "Invalid price per share: $price / $pricingType"
    )

    data class FiscalYearMismatch(val fiscalYearId: String, val shareOfferId: String): ShareException(
        "ShareOffer $shareOfferId not present in fiscalYear $fiscalYearId"
    )

    data class InvalidNumberOfShares(val number: Int): ShareException(
        "Invalid number of shares: $number"
    )

    data object ProviderMismatch: ShareException("Provider mismatch")
}
