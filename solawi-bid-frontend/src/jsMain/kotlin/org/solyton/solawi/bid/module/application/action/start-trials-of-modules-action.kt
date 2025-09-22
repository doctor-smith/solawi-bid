package org.solyton.solawi.bid.module.application.action


import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.StartTrialsOfModules
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.personalApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

fun startTrialsOfModules(moduleIds: List<String>): Action<ApplicationManagement, StartTrialsOfModules, ApiApplications> =
    Action<ApplicationManagement, StartTrialsOfModules, ApiApplications>(
        name = "START_TRIALS_OF_MODULES",
        reader = { _ -> StartTrialsOfModules(moduleIds) },
        endPoint = StartTrialsOfModules::class,
        writer = personalApplications.set contraMap {
                applications: ApiApplications -> applications.toDomainType()
        }
    )
