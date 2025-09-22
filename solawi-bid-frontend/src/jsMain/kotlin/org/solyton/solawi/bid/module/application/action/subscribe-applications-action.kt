package org.solyton.solawi.bid.module.application.action


import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.SubscribeApplications
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.personalApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

fun subscribeApplications(applicationIds: List<String>): Action<ApplicationManagement, SubscribeApplications, ApiApplications> =
    Action<ApplicationManagement, SubscribeApplications, ApiApplications>(
        name = "SUBSCRIBE_APPLICATIONS",
        reader = { _ -> SubscribeApplications(applicationIds) },
        endPoint = SubscribeApplications::class,
        writer = personalApplications.set contraMap {
                applications: ApiApplications -> applications.toDomainType()
        }
    )
