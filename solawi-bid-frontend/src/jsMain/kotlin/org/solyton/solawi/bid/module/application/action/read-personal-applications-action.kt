package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ReadPersonalUserApplications
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.personalApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

val readPersonalApplications: Action<ApplicationManagement, ReadPersonalUserApplications, ApiApplications> by lazy {
    Action<ApplicationManagement, ReadPersonalUserApplications, ApiApplications>(
        name = "READ_PERSONAL_APPLICATIONS",
        reader = { _ -> ReadPersonalUserApplications },
        endPoint = ReadPersonalUserApplications::class,
        writer = personalApplications.set contraMap {
                applications: ApiApplications -> applications.toDomainType()
        }
    )
}
