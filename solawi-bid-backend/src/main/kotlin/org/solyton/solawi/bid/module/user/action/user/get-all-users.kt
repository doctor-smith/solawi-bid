package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Action
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.user.data.api.GetUsers
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.data.api.Users
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.*
import org.solyton.solawi.bid.module.user.schema.User as UserEntity

/**
 * Get all users in the database
 */
// val GetAllUsers =
@MathDsl
val GetAllUsers: KlAction<Result<GetUsers>, Result<Users>> = KlAction{_ -> DbAction {
    database -> resultTransaction(database) {
    Users(UserEntity.all().map { userEntity ->
        User(
            userEntity.id.value.toString(),
            userEntity.username
        )
    })
    } x database
 } }

@MathDsl
val GetUserById: suspend (Result<UUID>)->Action<Result<UserEntity>> = {id -> DbAction {
    database -> id bindSuspend { uuid ->
        resultTransaction(database) {
            UserEntity.find {
                UsersTable.id eq uuid
            }.first()
        }
    } x database
} }



