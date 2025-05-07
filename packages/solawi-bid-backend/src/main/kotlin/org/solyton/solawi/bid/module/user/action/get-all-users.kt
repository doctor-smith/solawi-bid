package org.solyton.solawi.bid.module.user.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.Action
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.solyton.solawi.bid.module.db.schema.UsersTable
import org.solyton.solawi.bid.module.user.data.api.GetUsers
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.data.api.Users
import java.util.*
import kotlin.collections.first
import kotlin.collections.map
import org.solyton.solawi.bid.module.db.schema.User as UserEntity

/**
 * Get all users in the database
 */
// val GetAllUsers =
@MathDsl
val GetAllUsers: KlAction<Result<GetUsers>, Result<Users>> = KlAction{result -> DbAction {
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



