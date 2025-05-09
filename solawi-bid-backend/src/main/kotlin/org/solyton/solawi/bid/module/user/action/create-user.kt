package org.solyton.solawi.bid.module.user.action


import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.Context
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.solyton.solawi.bid.application.permission.Role
import org.solyton.solawi.bid.application.permission.Value
import org.solyton.solawi.bid.module.db.ContextException
import org.solyton.solawi.bid.module.db.PermissionException
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.service.hashPassword

@MathDsl
@Suppress("FunctionName")
val CreateNewUser: KlAction<Result<CreateUser>, Result<User>> = KlAction{ result ->DbAction {
    database -> result bindSuspend {data -> resultTransaction(database) {
        val user = UserEntity.find { UsersTable.username eq data.username }.firstOrNull()
        if(user != null) {
            User(user.id.value.toString(), user.username)
        } else {
            val applicationContext = ContextEntity.find { ContextsTable.name eq org.solyton.solawi.bid.application.permission.Context.Application.value }.firstOrNull()
                ?: throw ContextException.NoSuchRootContext(org.solyton.solawi.bid.application.permission.Context.Application.value)
            val applicationOrganizationContext = ContextEntity.find { ContextsTable.name eq Value.ORGANIZATION and (ContextsTable.rootId eq applicationContext.id) }.firstOrNull()
                ?: throw ContextException.NoSuchContext("${org.solyton.solawi.bid.application.permission.Context.Application.value}/${Value.ORGANIZATION}")
            val userRole = applicationContext.roles.find { it.name == Role.user.value }
                ?: throw PermissionException.NoSuchRole(Role.user.value)

            val userEntity = UserEntity.new{
                username = data.username
                password = hashPassword( data.password )
            }

            UserRoleContext.insert {
                it[userId] = userEntity.id.value
                it[roleId] = userRole.id.value
                it[contextId] = applicationOrganizationContext.id.value
            }

            User(userEntity.id.value.toString(), userEntity.username)
        }
    } } x database
} }