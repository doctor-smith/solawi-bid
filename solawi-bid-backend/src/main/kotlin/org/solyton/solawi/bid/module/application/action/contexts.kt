package org.solyton.solawi.bid.module.application.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.x
import org.solyton.solawi.bid.module.application.data.UpdateStandardApplicationContext
import org.solyton.solawi.bid.module.application.service.updateStandardApplicationContext
import org.solyton.solawi.bid.module.permission.data.api.Context

@Suppress("FunctionName")
fun UpdateStandardApplicationContext(): KlAction<Result<Contextual<UpdateStandardApplicationContext>>, Result<Context>> = KlAction{
    result -> DbAction {
        database-> result bindSuspend { contextual -> resultTransaction(database) {
            val data = contextual.data
            val application = data.applicationId
            val roles = data.roles

            val context = updateStandardApplicationContext(application, roles)
            context
        } }  x database
    }
}
