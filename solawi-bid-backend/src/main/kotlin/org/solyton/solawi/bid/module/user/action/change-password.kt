package org.solyton.solawi.bid.module.user.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.solyton.solawi.bid.module.db.schema.UserEntity
import org.solyton.solawi.bid.module.db.schema.UsersTable
import org.solyton.solawi.bid.module.user.data.api.ApiUser
import org.solyton.solawi.bid.module.user.data.api.ChangePassword
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.user.service.hashPassword
import java.util.UUID

@MathDsl
@Suppress("FunctionName")
val ChangePassword: KlAction<Result<ChangePassword>, Result<User>> = KlAction{ result ->
    DbAction { database -> result bindSuspend {data -> resultTransaction(database) {
        val user = UserEntity.find { UsersTable.username eq data.username }.firstOrNull()
            ?:throw UserManagementException.UserDoesNotExist.Username(data.username)

        user.password = hashPassword(data.password)
        ApiUser(user.id.value.toString(), user.username)
    } } x database
} }
