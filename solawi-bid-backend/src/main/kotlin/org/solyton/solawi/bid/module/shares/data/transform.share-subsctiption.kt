package org.solyton.solawi.bid.module.shares.data

import org.evoleq.exposedx.joda.toKotlinxWithZone
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.shares.schema.ShareTypes.providerId

fun List<ShareSubscriptionEntity>.toApiType(): ApiShareSubscriptions = ApiShareSubscriptions(
    map { it.toApiType() }
)

fun ShareSubscriptionEntity.toApiType(): ApiShareSubscription = ApiShareSubscription(
    id = id.value.toString(),
    providerId = providerId.toString(),
    fiscalYearId = fiscalYear.id.value.toString(),
    shareOfferId = shareOffer.id.value.toString(),
    userProfileId = userProfile.id.value.toString(),
    distributionPointId = distributionPoint?.id?.value.toString(),
    numberOfShares = numberOfShares,
    pricePerShare = pricePerShare,
    ahcAuthorized = ahcAuthorized,
    status = ShareStatus.from(status.name).toApiType(),
    coSubscribers = coSubscribers.map { it.user.username },
    statusUpdatedAt = statusUpdatedAt.toKotlinxWithZone(),
)
