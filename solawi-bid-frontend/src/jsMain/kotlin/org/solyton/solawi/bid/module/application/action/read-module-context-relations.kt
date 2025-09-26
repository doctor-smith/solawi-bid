package org.solyton.solawi.bid.module.application.action


import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApiModuleContextRelations
import org.solyton.solawi.bid.module.application.data.ReadPersonalModuleContextRelations
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.personalModuleContextRelations
import org.solyton.solawi.bid.module.application.data.toDomainType

val readModuleContextRelations: Action<ApplicationManagement, ReadPersonalModuleContextRelations, ApiModuleContextRelations> by lazy {
    Action<ApplicationManagement, ReadPersonalModuleContextRelations, ApiModuleContextRelations>(
        name = "READ_PERSONAL_MODULE_CONTEXT_RELATIONS",
        reader = { _ -> ReadPersonalModuleContextRelations },
        endPoint = ReadPersonalModuleContextRelations::class,
        writer = personalModuleContextRelations.set contraMap {
                applicationContexts: ApiModuleContextRelations -> applicationContexts.toDomainType()
        }
    )
}
