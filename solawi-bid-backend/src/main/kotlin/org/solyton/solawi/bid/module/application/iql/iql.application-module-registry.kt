package org.solyton.solawi.bid.module.application.iql

import org.evoleq.exposedx.iql.FieldNameStrategy
import org.evoleq.exposedx.iql.Registry
import org.evoleq.exposedx.iql.mapsTo
import org.evoleq.exposedx.iql.registry
import org.evoleq.iql.data.FieldType
import org.jetbrains.exposed.sql.EntityIDColumnType
import org.jetbrains.exposed.sql.jodatime.DateColumnType
import org.jetbrains.exposed.sql.jodatime.DateTimeWithTimeZoneColumnType
import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.auditable.iql.auditableFields
import org.solyton.solawi.bid.module.permission.iql.permissionModuleRegistry
import org.solyton.solawi.bid.module.permission.schema.ContextsTable

val applicationModuleRegistry: Registry by lazy {
    registry {
        fieldNameStrategy = FieldNameStrategy.SNAKE_CASE

        include(permissionModuleRegistry)

        fieldTypes(
            DateColumnType::class mapsTo FieldType.DATE,
            DateTimeWithTimeZoneColumnType::class mapsTo FieldType.DATETIME,
            EntityIDColumnType::class mapsTo FieldType.UUID,
        )

        entity("application", ApplicationsTable) {
            field(ApplicationsTable.name)
            field(ApplicationsTable.description)
            field(ApplicationsTable.isMandatory)
            field(ApplicationsTable.defaultContextId)

            auditableFields(ApplicationsTable)

            oneToMany("modules", ModulesTable) {
                ApplicationsTable.id references ModulesTable.applicationId
            }

            manyToOne("defaultContext", ContextsTable) {
                ApplicationsTable.defaultContextId references ContextsTable.id
            }
        }

        entity("module", ModulesTable) {
            field(ModulesTable.name)
            field(ModulesTable.description)
            field(ModulesTable.isMandatory)

            auditableFields(ModulesTable)

            manyToOne("application", ApplicationsTable) {
                ModulesTable.applicationId references ApplicationsTable.id
            }

            manyToOne("defaultContext", ContextsTable) {
                ModulesTable.defaultContextId references ContextsTable.id
            }
        }

        entity("userApplicationContext", UserApplicationsTable){
            field(UserApplicationsTable.userId)
            field(UserApplicationsTable.applicationId)
            field(UserApplicationsTable.contextId)

            field(UserApplicationsTable.lifecycleStageId)

            auditableFields(UserApplicationsTable)

            manyToOne("lifecycleStage", LifecycleStagesTable) {
                UserApplicationsTable.lifecycleStageId references LifecycleStagesTable.id
            }
        }

        entity("userModuleContext", UserModulesTable) {
            field(UserModulesTable.userId)
            field(UserModulesTable.moduleId)
            field(UserModulesTable.contextId)

            field(UserModulesTable.lifecycleStageId)

            auditableFields(UserModulesTable)

            manyToOne("lifecycleStage", LifecycleStagesTable) {
                UserModulesTable.lifecycleStageId references LifecycleStagesTable.id
            }
        }








        entity("lifeCycleStage", LifecycleStagesTable) {
            field(LifecycleStagesTable.name)
            field(LifecycleStagesTable.description)

            auditableFields(LifecycleStagesTable)
        }
    }
}
