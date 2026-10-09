package org.solyton.solawi.bid.module.shares.data

import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptionStatusHistories
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptionStatusHistory
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptionStatusHistoryEntry
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionStatusHistoryEntry
import org.solyton.solawi.bid.module.values.UserId

fun ShareSubscriptionStatusHistoryEntry.toApiType(): ApiShareSubscriptionStatusHistoryEntry = ApiShareSubscriptionStatusHistoryEntry(
    rollingOverFromShareSubscriptionId  = rollingOverFromShareSubscription?.let { ShareSubscriptionId(it.id.value.toString()) },
    fromStatus = fromStatus?.let{ ShareStatus.from(it.name).toApiType() },
    toStatus = ShareStatus.from(toStatus.name).toApiType(),
    reason = reason.toApiType(),
    changedBy = changedBy.toApiType(),
    comment = comment,
    humanModifierId = humanModifierId?.let{ UserId(it.toString()) }
)

fun List<ShareSubscriptionStatusHistoryEntry>.toApiType(): ApiShareSubscriptionStatusHistories =
    groupBy { it.shareSubscription.id.value }.map {
        entry -> ApiShareSubscriptionStatusHistory(
            ShareSubscriptionId(entry.key.toString()),
            entry.value.map { it.toApiType() }
        )
    }.let{
        ApiShareSubscriptionStatusHistories(it)
    }


