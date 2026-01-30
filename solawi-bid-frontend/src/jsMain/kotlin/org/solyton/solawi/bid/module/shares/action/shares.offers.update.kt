package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.bid.data.api.PricingType
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareOffer
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareOffer
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareOffers

/**
 * Updates the details of an existing share offer.
 *
 * @param shareOfferId The unique identifier of the share offer to be updated.
 * @param providerId The unique identifier of the provider associated with the share offer.
 * @param shareTypeId The identifier of the share type associated with the share offer.
 * @param fiscalYearId The unique identifier of the fiscal year linked to the share offer.
 * @param price The price of the share offer, or null if not applicable.
 * @param pricingType The type of pricing for the share offer, which can be either FIXED or FLEXIBLE.
 * @param ahcAuthorizationRequired A flag indicating whether AHC authorization is required for the share offer.
 * @param nameSuffix An optional suffix appended to the action's name (defaults to an empty string).
 * @return An `Action` instance that updates the specified share offer, defining the input, endpoint, and writer for the operation.
 */
fun updateShareOffer(
    shareOfferId: String,
    providerId: String,
    shareTypeId: String,
    fiscalYearId: String,
    price: Double?,
    pricingType: PricingType,
    ahcAuthorizationRequired: Boolean,
    nameSuffix: String = ""
): Action<ShareManagement, UpdateShareOffer, ApiShareOffer> = Action(
    name = "UpdateShareOffer$nameSuffix",
    reader = { _ ->
        UpdateShareOffer(
            id = shareOfferId,
            providerId = providerId,
            shareTypeId = shareTypeId,
            fiscalYearId = fiscalYearId,
            price = price,
            pricingType = pricingType,
            ahcAuthorizationRequired = ahcAuthorizationRequired
        )
    },
    endPoint = UpdateShareOffer::class,
    writer = shareOffers.update {
        p, q -> p.shareOfferId == q.shareOfferId
    } contraMap { sT: ApiShareOffer -> sT.toDomainType()}
)
