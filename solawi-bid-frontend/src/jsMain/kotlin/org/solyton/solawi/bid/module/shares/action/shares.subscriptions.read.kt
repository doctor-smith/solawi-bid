package org.solyton.solawi.bid.module.shares.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ReadPersonalShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ReadShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.personalShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toDomainType

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


const val READ_PERSONAL_SHARE_SUBSCRIPTIONS = "ReadPersonalShareSubscriptions"

fun readPersonalShareSubscriptions(
    nameSuffix: String = ""
) : Action<ShareManagement, ReadPersonalShareSubscriptions, ApiShareSubscriptions> = Action(
    name = READ_PERSONAL_SHARE_SUBSCRIPTIONS.suffixed(nameSuffix),
    reader = { ReadPersonalShareSubscriptions(listOf()) },
    endPoint = ReadPersonalShareSubscriptions::class,
    writer = personalShareSubscriptions.set contraMap {sT -> sT.toDomainType()}
)
