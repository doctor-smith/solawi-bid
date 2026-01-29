package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.bid.data.api.ApiShareSubscription
import org.solyton.solawi.bid.module.bid.data.api.UpdateShareSubscription
import org.solyton.solawi.bid.module.bid.data.shares.management.ShareManagement
import org.solyton.solawi.bid.module.bid.data.shares.management.shareSubscriptions
import org.solyton.solawi.bid.module.bid.data.toDomainType

/**
 * Updates an existing share subscription with the provided details.
 *
 * @param shareSubscriptionId The unique identifier of the share subscription to be updated.
 * @param providerId The unique identifier of the provider associated with the share subscription.
 * @param shareOfferId The unique identifier of the share offer linked to the subscription.
 * @param userProfileId The unique identifier of the user who owns the subscription.
 * @param distributionPointId An optional identifier for the distribution point associated with the subscription.
 * @param fiscalYearId The unique identifier of the fiscal year linked to the share subscription.
 * @param numberOfShares The number of shares to be updated for the subscription.
 * @param pricePerShare An optional price per share for the subscription, if applicable.
 * @param ahcAuthorized An optional flag indicating whether AHC authorization is granted.
 * @param coSubscribers A list of co-subscribers associated with the subscription. Defaults to an empty list.
 * @param nameSuffix A suffix appended to the action's name. Defaults to an empty string.
 * @return An Action instance that encapsulates the update operation, defining the input, endpoint, and writer for the share subscription update process.
 */
fun updateShareSubscription(
    shareSubscriptionId: String,
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
): Action<ShareManagement, UpdateShareSubscription, ApiShareSubscription> = Action(
    name = "UpdateShareSubscription$nameSuffix",
    reader = { _ ->
        UpdateShareSubscription(
            id = shareSubscriptionId,
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
    endPoint = UpdateShareSubscription::class,
    writer = shareSubscriptions.update {
        p, q -> p.shareSubscriptionId == q.shareSubscriptionId
    } contraMap {
        sT: ApiShareSubscription -> sT.toDomainType()
    }
)
