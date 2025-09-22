package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ReadApplications
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

val readApplications: Action<ApplicationManagement, ReadApplications, ApiApplications> by lazy {
    Action<ApplicationManagement, ReadApplications, ApiApplications>(
        name = "READ_APPLICATIONS",
        reader = { _ -> ReadApplications },
        endPoint = ReadApplications::class,
        writer = availableApplications.set contraMap {
            applications: ApiApplications -> applications.toDomainType()
        }
    )
}
