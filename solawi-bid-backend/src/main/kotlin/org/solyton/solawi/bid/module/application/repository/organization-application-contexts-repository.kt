package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelation
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelations
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextEntity
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.permission.schema.repository.cloneRightRoleContext
import org.solyton.solawi.bid.module.permission.schema.repository.createRootContext
import java.util.*

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
    userId: UUID,
) : ApplicationOrganizationRelations {

    val userContextIds = UserRoleContext.select(UserRoleContext.contextId)
        .where { UserRoleContext.userId eq userId }
        .distinct().map { row -> row[UserRoleContext.contextId] }

    val organizationApplications = OrganizationApplicationContextEntity.find {
        OrganizationApplicationContextsTable.contextId inList userContextIds
    }.toList()

    val organizationModules = OrganizationModuleContextEntity.find {
        OrganizationModuleContextsTable.contextId inList  userContextIds
    }.toList()

    val applicationModules:(applicationId: UUID,organizationId: UUID)->List<String> = {applicationId: UUID,organizationId: UUID ->
        organizationModules.filter{
            it.module.application.id.value == applicationId && it.organizationId == organizationId
        }.map{
            it.module.id.value.toString()
        }
    }

    val relations = organizationApplications.map {
        ApplicationOrganizationRelation(
            applicationId = it.application.id.value.toString(),
            organizationId = it.organizationId.toString(),
            contextId = it.context.id.value.toString(),
            moduleIds = applicationModules(it.application.id.value, it.organizationId)
        )
    }
    return ApplicationOrganizationRelations(relations)
}
