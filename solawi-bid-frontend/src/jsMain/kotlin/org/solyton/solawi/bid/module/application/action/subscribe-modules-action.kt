package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.SubscribeModules
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.personalApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

fun subscribeModules(moduleIds: List<String>): Action<ApplicationManagement, SubscribeModules, ApiApplications> =
    Action<ApplicationManagement, SubscribeModules, ApiApplications>(
        name = "SUBSCRIBE_MODULES",
        reader = { _ -> SubscribeModules(moduleIds) },
        endPoint = SubscribeModules::class,
        writer = personalApplications.set contraMap {
            applications: ApiApplications -> applications.toDomainType()
        }
    )
