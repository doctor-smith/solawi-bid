package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ReadShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions

const val READ_SHARE_SUBSCRIPTIONS = "ReadShareSubscriptions"

fun readShareSubscriptions(
    providerId: String,
    nameSuffix: String = ""
) : Action<ShareManagement, ReadShareSubscriptions, ApiShareSubscriptions> = Action(
    name = READ_SHARE_SUBSCRIPTIONS.suffixed(nameSuffix),
    reader = { ReadShareSubscriptions(listOf("provider" to providerId)) },
    endPoint = ReadShareSubscriptions::class,
    writer = shareSubscriptions.set contraMap {sT -> sT.toDomainType()}
)
