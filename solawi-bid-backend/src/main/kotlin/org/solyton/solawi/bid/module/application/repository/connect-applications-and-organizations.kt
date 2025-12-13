package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.name
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextsTable
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextEntity
import org.solyton.solawi.bid.module.application.schema.UserApplicationEntity
import org.solyton.solawi.bid.module.application.schema.UserApplicationsTable
import org.solyton.solawi.bid.module.application.schema.UserModuleEntity
import org.solyton.solawi.bid.module.application.schema.UserModulesTable
import org.solyton.solawi.bid.module.permission.schema.repository.cloneRightRoleContext
import org.solyton.solawi.bid.module.permission.schema.repository.createChild
import org.solyton.solawi.bid.module.permission.schema.repository.createRootContext
import java.util.*

/**
 * This function can only be used by the owner of the underlying application.
 */
fun Transaction.connectApplicationToOrganization(
    applicationId: UUID,
    organizationId: UUID,
    moduleIds: List<UUID>,
    userId: UUID
): ApplicationOrganizationRelations {
    // validation
    val application = ApplicationEntity.find { ApplicationsTable.id eq applicationId }.firstOrNull()
        ?: throw ApplicationException.NoSuchApplication(applicationId.toString())

    // if the user-application exists, the user is necessarily the owner!
    // so this step can be viewed as a permission chack
    val userApplication = UserApplicationEntity.find {
        UserApplicationsTable.userId eq userId and (UserApplicationsTable.applicationId eq applicationId)
    }.firstOrNull()?: throw ApplicationException.UserNotRegisteredForApplication(
        "$userId",
        "$applicationId"
    )

    val isAlreadyConnected = !OrganizationApplicationContextEntity.find {
        OrganizationApplicationContextsTable.applicationId eq applicationId and
        (OrganizationApplicationContextsTable.organizationId eq organizationId)
    }.empty()

    if(isAlreadyConnected) throw ApplicationException.AlreadyConnectedToOrganization(
        "$organizationId",
        "$applicationId"
    )

    // Create context
    val applicationContext = createRootContext(application.buildOrganizationApplicationContextName(organizationId))
    cloneRightRoleContext(
        application.defaultContext.id.value,
        applicationContext.id.value
    )
    createUserRoleContext(userId, "OWNER", applicationContext.id.value)
    OrganizationApplicationContextEntity.new {
        this.organizationId = organizationId
        this.application = application
        this.context = applicationContext
        lifecycleStage = userApplication.lifecycleStage
        createdBy = userId
    }

    // Add modules and transfer lifecycle-stages
    val allowedUserModules = UserModuleEntity.find {
        UserModulesTable.userId eq userId and (UserModulesTable.moduleId inList moduleIds)
    }.distinct().toList().filter {
        // todo:dev review list of allowed states
        it.lifecycleStage.name in listOf(
            LifecycleStage.Registered.name(),
            LifecycleStage.Trialing.name(),
            LifecycleStage.Active.name()
        )
    }

    // validate that user has appropriate permissions on all relevant module
    val forbiddenModules = moduleIds
        .filter { it !in allowedUserModules.map { userModule -> userModule.module.id.value } }
        .map { it.toString() }
        .toSet()

    if(forbiddenModules.isNotEmpty()) throw ApplicationException.UserNotRegisteredForModules(
        "$userId", forbiddenModules
    )

    allowedUserModules.forEach { userModule ->
        val moduleContext = applicationContext.createChild(userModule.module.buildOrganizationModuleContextName(organizationId))
        cloneRightRoleContext(
            userModule.module.defaultContext.id.value,
            moduleContext.id.value
        )
        createUserRoleContext(userId, "OWNER", moduleContext.id.value)

        OrganizationModuleContextEntity.new {
            this.organizationId = organizationId
            this.lifecycleStage = userModule.lifecycleStage
            this.module = userModule.module
            this.context = moduleContext
            this.createdBy = userId
        }
    }

    commit()

    return getApplicationOrganizationRelations(userId)
}
