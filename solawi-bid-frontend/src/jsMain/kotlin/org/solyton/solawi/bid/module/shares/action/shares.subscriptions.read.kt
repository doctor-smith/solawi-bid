package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ReadShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions

fun readShareSubscriptions(
    providerId: String,
    nameSuffix: String = ""
) : Action<ShareManagement, ReadShareSubscriptions, ApiShareSubscriptions> = Action(
    name = "ReadShareSubscriptions$nameSuffix",
    reader = { ReadShareSubscriptions(listOf("provider" to providerId)) },
    endPoint = ReadShareSubscriptions::class,
    writer = shareSubscriptions.set contraMap {sT -> sT.toDomainType()}
)
