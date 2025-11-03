package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import org.solyton.solawi.bid.module.application.schema.LifecycleStages
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextsTable
import org.solyton.solawi.bid.module.permission.schema.repository.cloneRightRoleContext
import org.solyton.solawi.bid.module.permission.schema.repository.createRootContext
import java.util.UUID

fun Transaction.registerOrganizationForModule(
    organizationId: UUID,
    moduleId: UUID
): OrganizationModuleContextEntity {
    val module = ModuleEntity.find { ModulesTable.id eq moduleId }.firstOrNull()
        ?: throw ApplicationException.NoSuchModule(moduleId.toString())

    val registrationPossible = OrganizationModuleContextEntity.find {
        (OrganizationModuleContextsTable.moduleId eq moduleId) and
                (OrganizationModuleContextsTable.organizationId eq organizationId)
    }.empty()

    if (!registrationPossible) {
        throw ApplicationException.ModuleRegistrationImpossible(
            organizationId.toString(),
            moduleId.toString()
        )
    }

    val registeredStage = LifecycleStageEntity.find { LifecycleStages.name eq "REGISTERED" }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage("REGISTERED")

    val context = createRootContext(module.buildOrganizationModuleContextName(organizationId))
    cloneRightRoleContext(
        module.defaultContext.id.value,
        context.id.value
    )
    createUserRoleContext(organizationId, "OWNER", context.id.value)

    return OrganizationModuleContextEntity.new {
        this.organizationId = organizationId
        this.module = module
        this.context = context
        this.lifecycleStage = registeredStage
        this.createdBy = organizationId
    }
}

fun Transaction.moveLifecycleStage(
    module: OrganizationModuleContextEntity,
    toId: UUID,
    modifierId: UUID
): OrganizationModuleContextEntity {
    val toStage = transitionTargetOf(module.lifecycleStage.id.value, toId)

    module.lifecycleStage = toStage
    module.modifiedBy = modifierId
    module.modifiedAt = DateTime.now()

    return module
}
