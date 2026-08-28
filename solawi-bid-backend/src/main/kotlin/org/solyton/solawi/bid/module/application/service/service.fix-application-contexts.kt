package org.solyton.solawi.bid.module.application.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import java.util.*

fun Transaction.fixApplicationContexts(): List<ApplicationEntity> {

    val userApplications = UserApplicationEntity.all()

    userApplications.forEach { userApplication ->
        fixContext(userApplication.application.defaultContext.id.value, userApplication.context.id.value)
    }

    val userModules = UserModuleEntity.all()

    userModules.forEach { userModule ->
        fixContext(userModule.module.defaultContext.id.value, userModule.context.id.value)
    }

    val organizationApplications = OrganizationApplicationContextEntity.all()

    organizationApplications.forEach { organizationApplication ->
        fixContext(organizationApplication.application.defaultContext.id.value, organizationApplication.context.id.value)
    }

    val organizationModules = OrganizationApplicationContextEntity.all()

    organizationModules.forEach { organizationModule ->
        fixContext(organizationModule.application.defaultContext.id.value, organizationModule.context.id.value)
    }

    return ApplicationEntity.all().toList()
}

fun Transaction.fixContexts(applicationId: UUID): ApplicationEntity {
    val application = ApplicationEntity.findById(applicationId) ?: throw ApplicationException.NoSuchApplication(applicationId.toString())
    val userApplications = UserApplicationEntity.find{
        UserApplicationsTable.applicationId eq applicationId
    }.toList()

    // ...

    return application
}

fun Transaction.fixContext(sourceContextId: UUID, targetContextId: UUID) {
    val sourceRrc = RoleRightContexts.selectAll().where {
        RoleRightContexts.contextId eq sourceContextId
    }.map { Triple(
        it[RoleRightContexts.roleId],
        it[RoleRightContexts.rightId],
        it[RoleRightContexts.contextId]
    ) }

    val targetRrc = RoleRightContexts.selectAll().where {
        RoleRightContexts.contextId eq targetContextId
    }.map { Triple(
        it[RoleRightContexts.roleId],
        it[RoleRightContexts.rightId],
        it[RoleRightContexts.contextId]
    ) }


    val rrcToRemove = targetRrc.filterNot { it in sourceRrc }
    val rrcToAdd = sourceRrc.filterNot { it in targetRrc }

    RoleRightContexts.deleteWhere {
        RoleRightContexts.contextId eq targetContextId and (
            RoleRightContexts.roleId inList rrcToRemove.map { rrc -> rrc.first }
        ) and (
            RoleRightContexts.rightId inList rrcToRemove.map { rrc -> rrc.second }
        )
    }

    rrcToAdd.forEach { rrc ->
        RoleRightContexts.insert {
            it[roleId] = rrc.first
            it[rightId] = rrc.second
            it[contextId] = targetContextId
        }
    }
}
