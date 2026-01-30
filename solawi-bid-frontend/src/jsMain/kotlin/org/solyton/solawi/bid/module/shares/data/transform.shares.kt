package org.solyton.solawi.bid.module.shares.data

import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.api.*
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.types.ShareType

/**
 * Converts an instance of ApiShareTypes to a list of ShareType domain objects.
 *
 * @return a list of ShareType objects created by mapping each ApiShareType to its domain representation.
 */
fun ApiShareTypes.toDomainType(): List<ShareType> = all.map { it.toDomainType() }

/**
 * Converts an instance of ApiShareType to its corresponding domain type ShareType.
 *
 * @return A ShareType object containing data mapped from the ApiShareType instance.
 */
fun ApiShareType.toDomainType(): ShareType = ShareType(
    shareTypeId = id,
    providerId = providerId,
    name = name,
    description = description
)

/**
 * Converts an instance of ApiShareOffers to a corresponding domain type representation in the form of a list of ShareOffer.
 *
 * @return a list of ShareOffer objects, each transformed from the corresponding ApiShareOffer within the ApiShareOffers instance.
 */
fun ApiShareOffers.toDomainType(): List<ShareOffer> = all.map { it.toDomainType() }

/**
 * Converts an instance of ApiShareOffer to its corresponding domain type, ShareOffer.
 *
 * @return A ShareOffer object containing the mapped properties from the ApiShareOffer instance.
 */
fun ApiShareOffer.toDomainType(): ShareOffer = ShareOffer(
    shareOfferId = id,
    shareType = shareType.toDomainType(),
    fiscalYear = fiscalYear.toDomainType(),
    price = price,
    pricingType = pricingType,
    ahcAuthorizationRequired = ahcAuthorizationRequired
)

/**
 * Converts an instance of ApiShareSubscriptions to a list of ShareSubscription domain objects.
 *
 * @return a List of ShareSubscription objects mapped from the ApiShareSubscriptions object.
 */
fun ApiShareSubscriptions.toDomainType(): List<ShareSubscription> = all.map { it.toDomainType() }

/**
 * Transforms an instance of ApiShareSubscription into a domain type ShareSubscription.
 *
 * @return a ShareSubscription instance with data mapped from the current ApiShareSubscription.
 */
fun ApiShareSubscription.toDomainType(): ShareSubscription = ShareSubscription(
    shareSubscriptionId = id,
    providerId = providerId,
    fiscalYearId = fiscalYearId,
    shareOfferId = shareOfferId,
    userProfileId = userProfileId,
    distributionPointId = distributionPointId,
    numberOfShares = numberOfShares,
    pricePerShare = pricePerShare,
    ahcAuthorized = ahcAuthorized,
    status = status.toDomainType(),
    coSubscribers = coSubscribers,
    statusUpdatedAt = statusUpdatedAt,
)

fun ApiShareStatus.toDomainType(): ShareStatus = when(this) {
    is org.solyton.solawi.bid.module.shares.data.api.ShareStatus.Paused -> ShareStatus.Paused
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.ActivationRejected -> ShareStatus.ActivationRejected
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.AwaitingAhcAuthorization -> ShareStatus.AwaitingAhcAuthorization
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.Cancelled -> ShareStatus.Cancelled
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.ClearedForAuction -> ShareStatus.ClearedForAuction
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.Expired -> ShareStatus.Expired
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.External -> ShareStatus.External
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.PaymentFailed -> ShareStatus.PaymentFailed
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.PendingActivation -> ShareStatus.PendingActivation
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.RolledOver -> ShareStatus.RolledOver
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.RollingOver -> ShareStatus.RollingOver
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.Subscribed -> ShareStatus.Subscribed
    org.solyton.solawi.bid.module.shares.data.api.ShareStatus.Suspended -> ShareStatus.Suspended
}
