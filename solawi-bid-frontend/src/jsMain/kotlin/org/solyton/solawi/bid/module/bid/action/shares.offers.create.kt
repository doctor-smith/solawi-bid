package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.bid.data.api.ApiShareOffer
import org.solyton.solawi.bid.module.bid.data.api.CreateShareOffer
import org.solyton.solawi.bid.module.bid.data.api.PricingType
import org.solyton.solawi.bid.module.bid.data.shares.management.ShareManagement
import org.solyton.solawi.bid.module.bid.data.shares.management.shareOffers
import org.solyton.solawi.bid.module.bid.data.toDomainType

fun createShareOffer(
    providerId: String,
    shareTypeId: String,
    fiscalYearId: String,
    price: Double?,
    pricingType: PricingType,
    ahcAuthorizationRequired: Boolean,
    nameSuffix: String = ""
): Action<ShareManagement, CreateShareOffer, ApiShareOffer> = Action(
    name = "CreateShareOffer$nameSuffix",
    reader = { _ ->
        CreateShareOffer(
            providerId = providerId,
            shareTypeId = shareTypeId,
            fiscalYearId = fiscalYearId,
            price = price,
            pricingType = pricingType,
            ahcAuthorizationRequired = ahcAuthorizationRequired
        )
    },
    endPoint = CreateShareOffer::class,
    writer = shareOffers.add() contraMap { sT: ApiShareOffer -> sT.toDomainType()}
)
