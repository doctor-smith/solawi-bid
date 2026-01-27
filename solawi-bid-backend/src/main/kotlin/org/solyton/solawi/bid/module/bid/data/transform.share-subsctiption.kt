package org.solyton.solawi.bid.module.bid.data

import org.evoleq.exposedx.joda.toKotlinxWithZone
import org.solyton.solawi.bid.module.bid.data.api.ApiShareSubscription
import org.solyton.solawi.bid.module.bid.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionEntity

fun List<ShareSubscriptionEntity>.toApiType(): ApiShareSubscriptions = ApiShareSubscriptions(
    map { it.toApiType() }
)

fun ShareSubscriptionEntity.toApiType(): ApiShareSubscription = ApiShareSubscription(
    id = id.value.toString(),
    shareOffer = shareOffer.toApiType(),
    numberOfShares = numberOfShares,
    pricePerShare = pricePerShare,
    ahcAuthorized = ahcAuthorized,
    distributionPointId = distributionPoint?.id?.value.toString(),
    userProfileId = userProfile.id.value.toString(),
    status = ShareStatus.from(status.name).toApiType(),
    statusUpdatedAt = statusUpdatedAt.toKotlinxWithZone(),
)
