package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.exposedx.iql.ExposedQueryCompiler
import org.evoleq.exposedx.iql.applyTo
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.iql.data.Query
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.x
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.data.api.UserQuery
import org.solyton.solawi.bid.module.user.data.api.Users
import org.solyton.solawi.bid.module.user.data.toApiType
import org.solyton.solawi.bid.module.user.iql.userModuleRegistry
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable


val UserQuery: KlAction<Result<Contextual<UserQuery>>, Result<Users>> = KlAction{ result -> DbAction{
        database -> result bindSuspend  {contextual: Contextual<UserQuery> ->

    resultTransaction(database) {
        val data = contextual.data
        val query: Query = data.query
        val compiledQuery = ExposedQueryCompiler(userModuleRegistry).compile(
            query = query,
            table = UsersTable
        )

        val users = compiledQuery.applyTo(UserEntity)

        Users(users.map { userEntity ->
            User(
                userEntity.id.value.toString(),
                userEntity.username,
                userEntity.status.toApiType()
            )
        })
    }
} x database
} }



