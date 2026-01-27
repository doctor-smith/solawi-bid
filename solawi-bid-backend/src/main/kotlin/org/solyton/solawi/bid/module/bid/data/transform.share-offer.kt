package org.solyton.solawi.bid.module.bid.data

import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.bid.data.api.ApiShareOffer
import org.solyton.solawi.bid.module.bid.data.api.ApiShareOffers
import org.solyton.solawi.bid.module.bid.schema.ShareOfferEntity

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
