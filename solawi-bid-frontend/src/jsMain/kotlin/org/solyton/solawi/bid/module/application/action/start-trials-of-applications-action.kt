package org.solyton.solawi.bid.module.application.action


import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.StartTrialsOfApplications
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.personalApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

fun startTrialsOfApplications(applicationIds: List<String>): Action<ApplicationManagement, StartTrialsOfApplications, ApiApplications> =
    Action<ApplicationManagement, StartTrialsOfApplications, ApiApplications>(
        name = "START_TRIALS_OF_APPLICATIONS",
        reader = { _ -> StartTrialsOfApplications(applicationIds) },
        endPoint = StartTrialsOfApplications::class,
        writer = personalApplications.set contraMap {
                applications: ApiApplications -> applications.toDomainType()
        }
    )
