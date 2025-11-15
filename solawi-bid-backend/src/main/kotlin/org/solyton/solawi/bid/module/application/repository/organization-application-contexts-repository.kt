package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelation
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelations
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import org.solyton.solawi.bid.module.application.schema.LifecycleStages
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContext
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextsTable
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextsTable
import org.solyton.solawi.bid.module.permission.schema.repository.cloneRightRoleContext
import org.solyton.solawi.bid.module.permission.schema.repository.createRootContext
import java.util.UUID

fun Transaction.registerOrganizationForApplication(
    organizationId: UUID,
    applicationId: UUID
): OrganizationApplicationContextEntity {
    val application = ApplicationEntity.find { ApplicationsTable.id eq applicationId }.firstOrNull()
        ?: throw ApplicationException.NoSuchApplication(applicationId.toString())

    val registrationPossible = OrganizationApplicationContextEntity.find {
        (OrganizationApplicationContextsTable.applicationId eq applicationId) and
                (OrganizationApplicationContextsTable.organizationId eq organizationId)
    }.empty()

    if (!registrationPossible) {
        throw ApplicationException.ApplicationRegistrationImpossible(
            organizationId.toString(),
            applicationId.toString()
        )
    }

    val registeredStage = LifecycleStageEntity.find { LifecycleStages.name eq "REGISTERED" }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage("REGISTERED")

    val context = createRootContext(application.buildOrganizationApplicationContextName(organizationId))
    cloneRightRoleContext(
        application.defaultContext.id.value,
        context.id.value
    )
    createUserRoleContext(organizationId, "OWNER", context.id.value)

    return OrganizationApplicationContextEntity.new {
        this.organizationId = organizationId
        this.application = application
        this.context = context
        this.lifecycleStage = registeredStage
        this.createdBy = organizationId
    }
}

fun Transaction.moveLifecycleStage(
    context: OrganizationApplicationContext,
    toId: UUID,
    modifierId: UUID
): OrganizationApplicationContext {
    val toStage = transitionTargetOf(context.lifecycleStage.id.value, toId)

    context.lifecycleStage = toStage
    context.modifiedBy = modifierId
    context.modifiedAt = DateTime.now()

    return context
}

fun Transaction.getApplicationOrganizationRelations(
    organizationId: UUID
) : ApplicationOrganizationRelations {

    val organizationModules = OrganizationModuleContextEntity.find {
        OrganizationModuleContextsTable.organizationId eq organizationId
    }.toList()

    val relations = organizationModules.groupBy {
        organizationModuleContext -> organizationModuleContext.module.application.id
    }.map { entry -> ApplicationOrganizationRelation(
        entry.key.value.toString(),
        organizationId.toString(),
        entry.value.map {
            organizationModuleContext -> organizationModuleContext.module.id.value.toString()
        }
    ) }
    return ApplicationOrganizationRelations(relations)
}
