package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
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
    val providerId: String,
    val shareTypeId: String,
    val fiscalYearId: String,
    val price: Double?,
    val pricingType: PricingType,
    val ahcAuthorizationRequired: Boolean
)

/**
 * Represents the parameters required to read share offers.
 *
 * This class extends the `Parameters` class and encapsulates the query parameters
 * necessary for fetching share offer details.
 *
 * @property queryParams Represents query parameters required for the request.  It requires the parameter: provider_id: UUID
 */
@Serializable
data class ReadShareOffers(override val queryParams: QueryParams): Parameters()

@Serializable
data class UpdateShareOffer(
    val id: String,
    val providerId: String,
    val shareTypeId: String,
    val fiscalYearId: String,
    val price: Double?,
    val pricingType: PricingType,
    val ahcAuthorizationRequired: Boolean
)
