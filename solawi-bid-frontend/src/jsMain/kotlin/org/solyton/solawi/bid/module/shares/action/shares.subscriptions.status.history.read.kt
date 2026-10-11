package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.math.x
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.updateAll
import org.solyton.solawi.bid.module.shares.data.api.ReadShareSubscriptionStatusHistory
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscriptionStatusHistories
import org.solyton.solawi.bid.module.shares.data.history.all
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptionStatusHistories
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId

const val READ_SHARE_SUBSCRIPTION_STATUS_HISTORIES = "READ_SHARE_SUBSCRIPTION_STATUS_HISTORIES"

fun readShareSubscriptionStatusHistories(
    subscriptionIds: List<ShareSubscriptionId>,
    nameSuffix: String? = null
): Action<ShareManagement, ReadShareSubscriptionStatusHistory, ShareSubscriptionStatusHistories> = Action(
    name = READ_SHARE_SUBSCRIPTION_STATUS_HISTORIES.suffixed(nameSuffix),
    reader = {ReadShareSubscriptionStatusHistory(subscriptionIds.map{
        "share_subscription_id" x it.value
    })},
    endPoint = ReadShareSubscriptionStatusHistory::class,
    writer = (shareSubscriptionStatusHistories * all).updateAll{
        p, q -> p.shareSubscriptionId == q.shareSubscriptionId
    } contraMap { histories -> histories.toDomainType().all }
)
