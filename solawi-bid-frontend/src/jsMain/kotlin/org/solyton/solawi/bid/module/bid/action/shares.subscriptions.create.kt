package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.bid.data.api.ApiShareSubscription
import org.solyton.solawi.bid.module.bid.data.api.CreateShareSubscription
import org.solyton.solawi.bid.module.bid.data.shares.management.ShareManagement
import org.solyton.solawi.bid.module.bid.data.shares.management.shareSubscriptions
import org.solyton.solawi.bid.module.bid.data.toDomainType

/**
 * Creates a subscription for a specific share offer by a user within a fiscal year.
 *
 * @param providerId The unique identifier of the provider offering the share.
 * @param shareOfferId The unique identifier of the share offer being subscribed to.
 * @param userProfileId The unique identifier of the user subscribing to the share.
 * @param distributionPointId An optional identifier for the distribution point related to the subscription.
 * @param fiscalYearId The unique identifier of the fiscal year within which the subscription applies.
 * @param numberOfShares The number of shares the user intends to subscribe to.
 * @param pricePerShare The optional price per individual share. If null, the default share price applies.
 * @param ahcAuthorized An optional authorization flag for AHC (Additional Handling Cost).
 * @param coSubscribers A list of additional co-subscribers included in the share subscription. Defaults to an empty list.
 * @param nameSuffix An optional suffix appended to the name of the action for custom identification. Defaults to an empty string.
 * @return An Action representing the creation of a share subscription, which includes the input data, endpoint, and writer logic.
 */
fun createShareSubscription(
    providerId: String,
    shareOfferId: String,
    userProfileId: String,
    distributionPointId: String?,
    fiscalYearId: String,
    numberOfShares: Int,
    pricePerShare: Double?,
    ahcAuthorized: Boolean?,
    coSubscribers: List<String> = emptyList(),
    nameSuffix: String = ""
): Action<ShareManagement, CreateShareSubscription, ApiShareSubscription> = Action(
    name = "CreateShareSubscription$nameSuffix",
    reader = { _ ->
        CreateShareSubscription(
            providerId = providerId,
            shareOfferId = shareOfferId,
            userProfileId = userProfileId,
            distributionPointId = distributionPointId,
            fiscalYearId = fiscalYearId,
            numberOfShares = numberOfShares,
            pricePerShare = pricePerShare,
            ahcAuthorized = ahcAuthorized,
            coSubscribers = coSubscribers
        )
    },
    endPoint = CreateShareSubscription::class,
    writer = shareSubscriptions.add() contraMap { sT: ApiShareSubscription -> sT.toDomainType()}
)
