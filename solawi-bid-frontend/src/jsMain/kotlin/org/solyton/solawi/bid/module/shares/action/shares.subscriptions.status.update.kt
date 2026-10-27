package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.updateAll
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatuses
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toDomainType

const val UPDATE_SHARE_STATUS = "UpdateShareStatus"

/**
 * Updates the share status of a share subscription for a specific provider.
 *
 * Important: The action also returns updates of companions, e.g., the rollover-companion!!
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
): Action<ShareManagement, UpdateShareStatus, ApiShareSubscriptions> = Action(
    name = UPDATE_SHARE_STATUS.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UpdateShareStatus::class,
    writer = shareSubscriptions.updateAll {
        p, q -> p.shareSubscriptionId == q.shareSubscriptionId
    } contraMap {
        shareSubscriptions: ApiShareSubscriptions -> shareSubscriptions.toDomainType()
    }
)

const val UPDATE_SHARE_STATUSES = "UPDATE_SHARE_STATUSES"

/**
 * Updates the statuses of share subscriptions by matching and replacing entries in the
 * existing share subscriptions data structure with the provided updated statuses.
 *
 * Important: The action also returns updates of companions, e.g., the rollover-companion!!
 *
 * @param data The `UpdateShareStatuses` object containing the provider ID and a list of
 * updated share statuses to be applied.
 * @param nameSuffix An optional suffix appended to the action's name to distinguish it
 * in specific contexts. Default is an empty string.
 * @return An `Action` instance that defines the operation for updating the statuses of
 * share subscriptions, including the input data, processing logic, and the writer
 * responsible for applying the updates to the share subscription records.
 */
fun updateShareStatuses(
    data: UpdateShareStatuses,
    nameSuffix: String = ""
): Action<ShareManagement, UpdateShareStatuses, ApiShareSubscriptions> = Action(
    name = UPDATE_SHARE_STATUSES.suffixed(nameSuffix),
    reader = { _ -> data },
    endPoint = UpdateShareStatuses::class,
    writer = shareSubscriptions.updateAll {
         p, q -> p.shareSubscriptionId == q.shareSubscriptionId
    } contraMap {
        shareSubscriptions: ApiShareSubscriptions -> shareSubscriptions.toDomainType()
    }
)
