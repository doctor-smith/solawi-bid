package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.data.api.organization.AddMember
import org.solyton.solawi.bid.module.user.data.api.organization.Organization
import org.solyton.solawi.bid.module.user.data.api.organization.RemoveMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateMember
import org.solyton.solawi.bid.module.user.data.toApiType
import org.solyton.solawi.bid.module.user.exception.OrganizationException
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.UUID

@MathDsl
@Suppress("FunctionName")
fun AddMember(): KlAction<Result<Contextual<AddMember>>, Result<Organization>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        val memberId = UUID.fromString(data.userId)

        val organization = OrganizationEntity.find { OrganizationsTable.id eq UUID.fromString(data.organizationId) }.firstOrNull()
            ?: throw OrganizationException.NoSuchOrganization(data.organizationId)

        val member = UserEntity.find { UsersTable.id eq memberId }.firstOrNull()
            ?: throw UserManagementException.UserDoesNotExist.Id(data.userId)

        if(!isGranted(
            userId,
            organization.context.id.value,
            "MANAGE_USERS"
        )) throw PermissionException.AccessDenied

        if(organization.members.toList().contains(member)) throw OrganizationException.DuplicateMember("$memberId")

        // add member
        UserOrganization.insert {
            it[UserOrganization.userId] = member.id
            it[UserOrganization.organizationId] = organization.id
        }

        // add roles to user-role-contexts
        data.roles.forEach { role ->
            UserRoleContext.insert {
                it[UserRoleContext.userId] = memberId
                it[UserRoleContext.roleId] = UUID.fromString(role)
                it[UserRoleContext.contextId] = organization.context.id.value
            }
        }

        // return
        organization.toApiType(this)
    } } x database
} }

@MathDsl
@Suppress("FunctionName")
fun RemoveMember(): KlAction<Result<Contextual<RemoveMember>>, Result<Organization>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        val memberId = UUID.fromString(data.userId)

        val organization = OrganizationEntity.find { OrganizationsTable.id eq UUID.fromString(data.organizationId) }.firstOrNull()
            ?: throw OrganizationException.NoSuchOrganization(data.organizationId)

        val user = UserEntity.find { UsersTable.id eq memberId }.firstOrNull()
            ?: throw UserManagementException.UserDoesNotExist.Id(data.userId)

        if(!isGranted(
            userId,
            organization.context.id.value,
            "MANAGE_USERS"
        )) throw PermissionException.AccessDenied

        // remove member
        UserOrganization.deleteWhere {
            UserOrganization.userId eq user.id and
            (UserOrganization.organizationId eq organization.id)
        }

        // remove roles from user-role-contexts
        UserRoleContext.deleteWhere {
            UserRoleContext.userId eq memberId and
            (UserRoleContext.contextId eq organization.context.id)
        }

        // return
        organization.toApiType(this)
    } } x database
} }

@MathDsl
@Suppress("FunctionName")
fun UpdateMember(): KlAction<Result<Contextual<UpdateMember>>, Result<Organization>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        val memberId = UUID.fromString(data.userId)

        val organization = OrganizationEntity.find { OrganizationsTable.id eq UUID.fromString(data.organizationId) }.firstOrNull()
            ?: throw OrganizationException.NoSuchOrganization(data.organizationId)

        if(!isGranted(
            userId,
            organization.context.id.value,
            "MANAGE_USERS"
        )) throw PermissionException.AccessDenied

        val roleIds = data.roles.map { UUID.fromString(it) }
        UserRoleContext.deleteWhere {
            UserRoleContext.contextId eq organization.context.id and (UserRoleContext.userId notInList roleIds)
        }
        val existingRoles = UserRoleContext.selectAll().where {
            UserRoleContext.userId eq memberId and (UserRoleContext.contextId eq organization.context.id)
        }.map { row -> row[UserRoleContext.roleId].value }

        roleIds.filter { it !in existingRoles }.forEach{ roleId ->
            UserRoleContext.insert {
                it[UserRoleContext.userId] = memberId
                it[UserRoleContext.roleId] = roleId
                it[UserRoleContext.contextId] = organization.context.id.value
            }
        }
        // return
        organization.toApiType(this)
    } } x database
} }



