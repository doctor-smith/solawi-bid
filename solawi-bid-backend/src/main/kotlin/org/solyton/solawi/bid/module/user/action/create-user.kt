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
            val userEntity = UserEntity.new{
                username = data.username
                password = hashPassword( data.password )
            }

            User(userEntity.id.value.toString(), userEntity.username)
        }
    } } x database
} }