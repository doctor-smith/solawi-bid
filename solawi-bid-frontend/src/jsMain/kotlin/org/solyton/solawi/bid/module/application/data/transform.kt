package org.solyton.solawi.bid.module.application.data

import org.solyton.solawi.bid.module.application.data.application.Application
import org.solyton.solawi.bid.module.application.data.module.Module
import org.solyton.solawi.bid.module.application.data.userapplication.UserApplications


fun ApiUserApplications.toDomainType(): List<UserApplications> = map.entries.map {
    entry -> UserApplications(
        userId =entry.key,
        applications = entry.value.map { application -> application.toDomainType() }
    )
}

fun ApiApplications.toDomainType(): List<Application> = list.map {
    application -> application.toDomainType()
}

fun ApiApplication.toDomainType(): Application = Application(
    id = id,
    name = name,
    state = lifecycleStage,
    modules = modules.toDomainType()
)

fun List<ApiModule>.toDomainType(): List<Module> = map { module -> module.toDomainType() }

fun ApiModule.toDomainType(): Module = Module(
    id = id,
    name = name,
    state = lifecycleStage
)

