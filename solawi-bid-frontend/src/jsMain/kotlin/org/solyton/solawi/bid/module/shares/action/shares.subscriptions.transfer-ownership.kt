package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.TransferOwnership
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserProfileId

const val TRANSFER_OWNERSHIP_OF_SHARE_SUBSCRIPTION = "TRANSFER_OWNERSHIP_OF_SHARE_SUBSCRIPTION"

/**
 * Transfers ownership of a share subscription to a new owner.
 *
 * @param providerId The unique identifier of the provider managing the share subscription.
 * @param shareSubscriptionId The unique identifier of the share subscription to be transferred.
 * @param newOwnerId The unique identifier of the user profile that will become the new owner of the share subscription.
 * @param nameSuffix An optional suffix appended to the action's name, used for distinguishing similar actions.
 * @return An `Action` that defines the transfer ownership operation, including the input, endpoint, and writer for updating the share subscription.
 */
fun transferOwnership(
    providerId: ProviderId,
    shareSubscriptionId: ShareSubscriptionId,
    newOwnerId: UserProfileId,
    nameSuffix: String? = null
): Action<ShareManagement, TransferOwnership, ShareSubscription> = Action(
    name = TRANSFER_OWNERSHIP_OF_SHARE_SUBSCRIPTION.suffixed(nameSuffix),
    reader = { TransferOwnership(providerId, shareSubscriptionId, newOwnerId) },
    endPoint = TransferOwnership::class,
    writer = shareSubscriptions.update {
        p, q -> p.shareSubscriptionId == q.shareSubscriptionId
    } contraMap {subscription -> subscription.toDomainType() }
)
