package org.solyton.solawi.bid.module.application.action


import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiUserApplications
import org.solyton.solawi.bid.module.application.data.ReadUserApplications
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.userApplications
import org.solyton.solawi.bid.module.application.data.toDomainType

fun readUserApplications(userIds: List<String>): Action<ApplicationManagement, ReadUserApplications, ApiUserApplications> =
    Action<ApplicationManagement, ReadUserApplications, ApiUserApplications>(
        name = "READ_USER_APPLICATIONS",
        reader = { _ -> ReadUserApplications(userIds) },
        endPoint = ReadUserApplications::class,
        writer = userApplications.set contraMap {
            applications: ApiUserApplications -> applications.toDomainType()
        }
    )
