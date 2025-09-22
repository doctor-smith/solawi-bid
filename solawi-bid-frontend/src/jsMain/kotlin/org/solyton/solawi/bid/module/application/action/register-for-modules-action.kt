package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.RegisterForModules
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.personalApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

fun registerForModules(moduleIds: List<String>): Action<ApplicationManagement, RegisterForModules, ApiApplications> =
    Action<ApplicationManagement, RegisterForModules, ApiApplications>(
        name = "REGISTER_FOR_MODULES",
        reader = { _ -> RegisterForModules(moduleIds) },
        endPoint = RegisterForModules::class,
        writer = personalApplications.set contraMap {
            applications: ApiApplications -> applications.toDomainType()
        }
    )
