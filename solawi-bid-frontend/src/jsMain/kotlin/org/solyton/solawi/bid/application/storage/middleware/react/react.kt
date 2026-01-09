package org.solyton.solawi.bid.application.storage.middleware.react

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.evoleq.math.Reader
import org.evoleq.math.dispatch
import org.evoleq.math.map
import org.evoleq.math.on
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.math.write
import org.evoleq.math.x
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.times
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.managedUsers
import org.solyton.solawi.bid.application.data.processes
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.page.user.action.READ_PARENT_CHILD_RELATIONS_OF_CONTEXT
import org.solyton.solawi.bid.application.ui.page.user.action.READ_USER_PERMISSIONS
import org.solyton.solawi.bid.application.ui.page.user.action.readParentChildRelationsOfContextsAction
import org.solyton.solawi.bid.application.ui.page.user.action.readRightRoleContextsAction
import org.solyton.solawi.bid.application.ui.page.user.action.readUserPermissionsAction
import org.solyton.solawi.bid.module.application.action.CONNECT_APPLICATION_TO_ORGANIZATION
import org.solyton.solawi.bid.module.application.action.readApplicationContextRelations
import org.solyton.solawi.bid.module.application.action.readApplications
import org.solyton.solawi.bid.module.application.action.readModuleContextRelations
import org.solyton.solawi.bid.module.application.action.readPersonalApplicationOrganizationContextRelations
import org.solyton.solawi.bid.module.application.action.readPersonalApplications
import org.solyton.solawi.bid.module.application.permission.Context
import org.solyton.solawi.bid.module.context.data.current
import org.solyton.solawi.bid.module.permission.data.api.Contexts
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.process.data.process.ProcessState
import org.solyton.solawi.bid.module.process.data.processes.SetStateOf
import org.solyton.solawi.bid.module.process.data.processes.SetStatesOf
import org.solyton.solawi.bid.module.process.data.processes.UnRegister
import org.solyton.solawi.bid.module.user.action.permission.readPermissionsOfUsersAction
import org.solyton.solawi.bid.module.user.action.user.GET_USERS
import org.solyton.solawi.bid.module.user.action.user.READ_USER_PROFILES


@MathDsl
@Suppress("FunctionName")
fun <S: Any, T: Any> React(action: Action<Application, S, T>): KlState<Storage<Application>, Result<T>, Result<T>> = {
    result -> State { storage ->
        if(result is Result.Success) with((storage * actions).read()) react@{
            when(action.name) {
                // POC for the current usecase (SMA-230)
                // 1. Read right role contexts of a user
                // 2. Read parent-child-relations and all other context-relations
                // 3. Read right role contexts of all returned contexts
                READ_USER_PERMISSIONS -> CoroutineScope(Job()).launch{
                    val contextId = (result.data as Contexts).list.first { it.name == Context.Application.value }.id
                    (storage * context * current).write(contextId)
                    // console.log("Emitting readParentChildRelationsOfContextsAction")
                    emit(readParentChildRelationsOfContextsAction("React"))
                    // Read other context-relations
                    emit(applicationManagementModule * readPersonalApplications )
                    emit(applicationManagementModule * readApplications )
                    emit(applicationManagementModule * readApplicationContextRelations )
                    emit(applicationManagementModule * readModuleContextRelations)
                    emit(applicationManagementModule * readPersonalApplicationOrganizationContextRelations())
                    // emit(userIso * readPermissionsOfUsersAction())
                }
                "${READ_PARENT_CHILD_RELATIONS_OF_CONTEXT}React" -> CoroutineScope(Job()).launch{
                    // console.log("Emitting readRightRoleContextsAction")
                    emit(readRightRoleContextsAction(
                        "React",
                        result.data as ParentChildRelationsOfContexts
                    ))
                }
                "${CONNECT_APPLICATION_TO_ORGANIZATION}React" -> CoroutineScope(Job()).launch {
                    emit(readUserPermissionsAction())
                }
                "${GET_USERS}ReactAndDeactivateProcess" -> CoroutineScope(Job()).launch {
                    (storage * processes * SetStateOf(
                        GET_USERS)
                            ) dispatch ProcessState.Inactive
                }
                "${READ_USER_PROFILES}ReactAndStopProcess" -> CoroutineScope(Job()).launch {
                    (storage * processes * SetStatesOf(
                        GET_USERS, READ_USER_PROFILES)
                    ) dispatch ProcessState.Finished
                }
                else -> Unit
                // One could also use this mechanism to establish pagination
            }
        }
        result x storage
    }
}
