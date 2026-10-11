package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.upsertAll
import org.solyton.solawi.bid.module.shares.data.api.RolloverShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.values.ShareOfferId
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserId

const val ROLLOVER_SHARE_SUBSCRIPTIONS = "ROLLOVER_SHARE_SUBSCRIPTIONS"

/**
 * Performs the rollover of share subscriptions from a given set of share subscriptions to a new share offer.
 *
 * @param providerId The unique identifier of the provider performing the rollover.
 * @param shareSubscriptionIds The list of share subscription IDs that are being rolled over.
 * @param toShareOfferId The ID of the target share offer to which the subscriptions are being moved.
 * @param changedBy Specifies who triggered the change (e.g., USER, PROVIDER, or SYSTEM).
 * @param modifiedBy The ID of the user who modified the subscriptions.
 * @param nameSuffix An optional suffix appended to the action's name.
 * @return An `Action` instance containing the details of the rollover operation, its input, and its output mapping for share subscriptions.
 */
fun rolloverShareSubscriptions(
    providerId: ProviderId,
    shareSubscriptionIds: List<ShareSubscriptionId>,
    toShareOfferId: ShareOfferId,
    changedBy: ChangedBy,
    modifiedBy: UserId,
    nameSuffix: String? = null
): Action<ShareManagement, RolloverShareSubscriptions, ShareSubscriptions> = Action(
    name = ROLLOVER_SHARE_SUBSCRIPTIONS.suffixed(nameSuffix),
    reader = { RolloverShareSubscriptions(
        providerId,
        shareSubscriptionIds,
        toShareOfferId,
        changedBy.toApiType(),
        modifiedBy
    ) },
    endPoint = RolloverShareSubscriptions::class,
    writer = shareSubscriptions.upsertAll {
        p, q -> p.shareSubscriptionId == q.shareSubscriptionId
    } contraMap {subscriptions: ShareSubscriptions -> subscriptions.all.map { it.toDomainType()}}
)
