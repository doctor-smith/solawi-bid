package org.solyton.solawi.bid.module.user.service.user

import org.evoleq.permission.Role
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.permission.exception.PermissionException
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.data.api.ApiUser
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.permission.ApplicationContext
import org.solyton.solawi.bid.module.user.permission.Value
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.user.service.bcrypt.hashPassword
import java.util.*

/**
 * Creates a new API user based on the provided user data and creator's ID.
 *
 * @param data The details of the user to be created, including username and password.
 * @param creatorId The unique identifier of the entity creating the user.
 * @return An instance of the created API user with its ID and username.
 */
fun Transaction.createUser(data: CreateUser, creatorId: UUID): ApiUser {
    val userEntity = createUserEntity(data, creatorId)
    return ApiUser(userEntity.id.value.toString(), userEntity.username)
}

/**
 * Creates a new user entity in the database or retrieves the existing user if a user
 * with the given username already exists. If a new user is created, they are assigned
 * a default role within the application's organization context.
 *
 * @param data The user creation details, including username and password.
 * @param creatorId The unique identifier of the entity creating the new user.
 * @return The created or existing user entity.
 * @throws ContextException.NoSuchRootContext If the application's root context cannot be found.
 * @throws ContextException.NoSuchContext If the application's organization context cannot be found.
 * @throws PermissionException.NoSuchRole If the default user role could not be found.
 */
fun Transaction.createUserEntity(data: CreateUser, creatorId: UUID): UserEntity {
    val user = UserEntity.find { UsersTable.username eq data.username }.firstOrNull()
    return if(user != null) {
        user
    } else {
        val applicationContext = ContextEntity.find { ContextsTable.name eq ApplicationContext.value }.firstOrNull()
            ?: throw ContextException.NoSuchRootContext(ApplicationContext.value)
        val applicationOrganizationContext =
            ContextEntity.find { ContextsTable.name eq Value.ORGANIZATION and (ContextsTable.rootId eq applicationContext.id) }
                .firstOrNull()
                ?: throw ContextException.NoSuchContext("${ApplicationContext.value}/${Value.ORGANIZATION}")
        val userRole = applicationContext.roles.find { it.name == Role.user.value }
            ?: throw PermissionException.NoSuchRole(Role.user.value)

        val userEntity = UserEntity.new {
            username = data.username
            password = hashPassword(data.password)
            createdBy = creatorId
        }

        UserRoleContext.insert {
            it[userId] = userEntity.id.value
            it[roleId] = userRole.id.value
            it[contextId] = applicationOrganizationContext.id.value
        }

        userEntity
    }
}
