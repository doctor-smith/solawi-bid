package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.user.data.api.ApiUser
import org.solyton.solawi.bid.module.user.data.api.UpdateUser
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.data.toApiType
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.user.service.bcrypt.credentialsAreOK
import org.solyton.solawi.bid.module.user.service.bcrypt.hashPassword


@MathDsl
@Suppress("FunctionName")
val UpdateUser: KlAction<Result<Contextual<UpdateUser>>, Result<User>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {contextual -> resultTransaction(database) {
        val modifierId = contextual.userId
        val data = contextual.data

        val user = UserEntity.find { UsersTable.username eq data.oldUsername.value }.firstOrNull()
            ?:throw UserManagementException.UserDoesNotExist.Username(data.oldUsername.value)

        val loginImpossible = user.status in setOf(UserStatus.REGISTERED, UserStatus.DISABLED, UserStatus.INVITED)
        if(loginImpossible) throw PermissionException.AccessDenied

        val oldPassword = requireNotNull(user.password)

        if(!credentialsAreOK(data.oldPassword.value, oldPassword ))
            throw UserManagementException.WrongCredentials

        // TODO Maybe validate user by email, let user confirm changes using the old address

        val usernameChanged = user.username != data.newUsername.value.lowercase().trim()
        val passwordChanged = !credentialsAreOK(data.newPassword.value, oldPassword)

        if(usernameChanged) {
            user.username = data.newUsername.value.lowercase().trim()
        }
        if(passwordChanged) {
            user.password = hashPassword(data.newPassword.value)
        }

        if(usernameChanged || passwordChanged) {
            user.modifiedBy = modifierId
            user.modifiedAt = DateTime.now()
        }

        ApiUser(user.id.value.toString(), user.username, user.status.toApiType())
    } } x database
    } }
