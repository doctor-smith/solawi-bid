package org.solyton.solawi.bid.module.application.data

import org.solyton.solawi.bid.module.application.data.application.Application
import org.solyton.solawi.bid.module.application.data.module.Module
import org.solyton.solawi.bid.module.application.data.userapplication.UserApplications
import org.solyton.solawi.bid.module.application.data.organizationrelation.ApplicationOrganizationRelation
import org.solyton.solawi.bid.module.permissions.data.relations.ContextRelation


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

fun ApiApplicationContextRelations.toDomainType(): List<ContextRelation> = all.map { it.toDomainType() }

fun ApiApplicationContextRelation.toDomainType(): ContextRelation = ContextRelation(
    contextId = contextId,
    relatedId = applicationId
)

fun ApiModuleContextRelations.toDomainType(): List<ContextRelation> = all.map { it.toDomainType() }

fun ApiModuleContextRelation.toDomainType(): ContextRelation = ContextRelation(
    contextId = contextId,
    relatedId = moduleId
)

fun ApiApplicationOrganizationRelations.toDomainType(): List<ApplicationOrganizationRelation> = all.map {
    applicationOrganizationRelation -> applicationOrganizationRelation.toDomainType()
}

fun ApiApplicationOrganizationRelation.toDomainType(): ApplicationOrganizationRelation =
    ApplicationOrganizationRelation(
        applicationId = applicationId,
        organizationId = organizationId,
        contextId = contextId,
        moduleIds = moduleIds
    )
