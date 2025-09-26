package org.solyton.solawi.bid.module.application.action


import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.management.personalApplicationContextRelations
import org.solyton.solawi.bid.module.application.data.ApiApplicationContextRelations
import org.solyton.solawi.bid.module.application.data.ReadPersonalApplicationContextRelations
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.toDomainType

val readApplicationContextRelations: Action<ApplicationManagement, ReadPersonalApplicationContextRelations, ApiApplicationContextRelations> by lazy {
    Action<ApplicationManagement, ReadPersonalApplicationContextRelations, ApiApplicationContextRelations>(
        name = "READ_PERSONAL_APPLICATION_CONTEXT_RELATIONS",
        reader = { _ -> ReadPersonalApplicationContextRelations },
        endPoint = ReadPersonalApplicationContextRelations::class,
        writer = personalApplicationContextRelations.set contraMap {
                applicationContexts: ApiApplicationContextRelations -> applicationContexts.toDomainType()
        }
    )
}
