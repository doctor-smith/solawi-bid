package org.solyton.solawi.bid.application.storage.middleware.react

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.x
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.ui.page.user.action.readParentChildRelationsOfContextsAction
import org.solyton.solawi.bid.application.ui.page.user.action.readRightRoleContextsAction
import org.solyton.solawi.bid.module.context.data.current
import org.solyton.solawi.bid.module.permission.data.api.Contexts
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContexts


@MathDsl
@Suppress("FunctionName")
fun <S: Any, T: Any> React(action: Action<Application, S, T>): KlState<Storage<Application>, Result<T>, Result<T>> = {
        result -> State { storage ->
    if(result is Result.Success) with((storage * actions).read()) react@{
        when(action.name) {
            // POC for the current usecase (SMA-230)
            // 1. Read right role contexts of a user
            // 2. Read parent-child-relations
            // 3. Read right role contexts of all returned contexts
            "ReadUserPermissions" -> CoroutineScope(Job()).launch{
                val contextId = (result.data as Contexts).list.first { it.name == "APPLICATION" }.id
                (storage * context * current).write(contextId)
                console.log("Emitting readParentChildRelationsOfContextsAction")
                emit(readParentChildRelationsOfContextsAction("React"))
            }
            "ReadParentChildRelationsOfContextsReact" -> CoroutineScope(Job()).launch{
                console.log("Emitting readParentChildRelationsOfContextsAction")
                emit(readRightRoleContextsAction(
                    "React",
                    result.data as ParentChildRelationsOfContexts
                ))
            }
            else -> Unit
            // One could also use this mechanism to establish pagination
        }
    }
    result x storage
}
}
