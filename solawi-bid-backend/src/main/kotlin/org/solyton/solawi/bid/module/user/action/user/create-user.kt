package org.solyton.solawi.bid.module.user.action.user


import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.user.service.user.createUser
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.User

@MathDsl
@Suppress("FunctionName")
val CreateNewUser: KlAction<Result<Contextual<CreateUser>>, Result<User>> = KlAction{ result ->DbAction {
    database -> result bindSuspend {contextual -> resultTransaction(database) {
        val creatorId = contextual.userId
        val data = contextual.data

        createUser(data, creatorId)
    } } x database
} }
