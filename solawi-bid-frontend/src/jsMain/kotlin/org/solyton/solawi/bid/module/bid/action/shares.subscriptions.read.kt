package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.bid.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.bid.data.api.ReadShareSubscriptions
import org.solyton.solawi.bid.module.bid.data.shares.management.ShareManagement
import org.solyton.solawi.bid.module.bid.data.shares.management.shareSubscriptions
import org.solyton.solawi.bid.module.bid.data.toDomainType

fun readShareSubscriptions(
    providerId: String,
    nameSuffix: String = ""
) : Action<ShareManagement, ReadShareSubscriptions, ApiShareSubscriptions> = Action(
    name = "ReadShareSubscriptions$nameSuffix",
    reader = { ReadShareSubscriptions(listOf("provider_id" to providerId)) },
    endPoint = ReadShareSubscriptions::class,
    writer = shareSubscriptions.set contraMap {sT -> sT.toDomainType()}
)
