package org.solyton.solawi.bid.module.application.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.x
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.application.data.FixApplicationRelatedContexts
import org.solyton.solawi.bid.module.application.data.FixModuleRelatedContexts
import org.solyton.solawi.bid.module.application.data.UpdateStandardApplicationContext
import org.solyton.solawi.bid.module.application.repository.validatedModule
import org.solyton.solawi.bid.module.application.service.fixContexts
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

@Suppress("FunctionName")
fun FixApplicationRelatedContexts(): KlAction<Result<Contextual<FixApplicationRelatedContexts>>, Result<Unit>> =
    KlAction{ result -> DbAction { database -> result bindSuspend {contextual ->  resultTransaction(database) {
        fixContexts(contextual.data.applicationId.value.toUuid())
        Unit
    } } x database }
    }


@Suppress("FunctionName")
fun FixModuleRelatedContexts(): KlAction<Result<Contextual<FixModuleRelatedContexts>>, Result<Unit>> =
    KlAction{ result -> DbAction { database -> result bindSuspend {contextual ->  resultTransaction(database) {
        val module = validatedModule(contextual.data.moduleId.value.toUuid())
        fixContexts(module.application.id.value)
        Unit
    } } x database }
    }
