package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toDomainType

const val UPDATE_SHARE_STATUS = "UpdateShareStatus"

/**
 * Updates the share status of a share subscription for a specific provider.
 *
 * @param data The input data encapsulated in an UpdateShareStatus object, including details such as the provider ID,
 *             share subscription ID, new state, and reason for the status change.
 * @param nameSuffix An optional suffix appended to the action's name.
 * @return An `Action` instance configured to update the share status, containing the corresponding input, endpoint type,
 *         and writer operation to apply the change to the storage.
 */
fun updateShareStatus(
    data: UpdateShareStatus,
    nameSuffix: String = ""
): Action<ShareManagement, UpdateShareStatus, ApiShareSubscription> = Action(
    name = UPDATE_SHARE_STATUS.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UpdateShareStatus::class,
    writer = shareSubscriptions.update {
        p, q -> p.shareSubscriptionId == q.shareSubscriptionId
    } contraMap {
        shareSubscription: ApiShareSubscription -> shareSubscription.toDomainType()
    }
)
