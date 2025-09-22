package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.RegisterForApplications
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.personalApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

fun registerForApplications(applicationIds: List<String>): Action<ApplicationManagement, RegisterForApplications, ApiApplications> =
    Action<ApplicationManagement, RegisterForApplications, ApiApplications>(
        name = "REGISTER_FOR_APPLICATIONS",
        reader = { _ -> RegisterForApplications(applicationIds) },
        endPoint = RegisterForApplications::class,
        writer = personalApplications.set contraMap {
            applications: ApiApplications -> applications.toDomainType()
        }
    )
