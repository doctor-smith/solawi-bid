package org.solyton.solawi.bid.module.shares.data

import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareOffer
import org.solyton.solawi.bid.module.shares.data.api.ApiShareOffers
import org.solyton.solawi.bid.module.shares.schema.ShareOfferEntity

fun List<ShareOfferEntity>.toApiType(): ApiShareOffers = ApiShareOffers(
    map{ it.toApiType() }
)

fun ShareOfferEntity.toApiType(): ApiShareOffer = ApiShareOffer(
    id = id.value.toString(),
    shareType = shareType.toApiType(),
    fiscalYear = fiscalYear.toApiType(),
    price = price,
    pricingType = pricingType.toApiType(),
    ahcAuthorizationRequired = ahcAuthorizationRequired
)
