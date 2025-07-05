package org.solyton.solawi.bid.module.permission.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.ReceiveContextual
import org.solyton.solawi.bid.application.action.io.Respond
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.module.permission.action.db.GetRoleRightContexts
import org.solyton.solawi.bid.module.permission.action.db.GetRoleRightContextsOfUsers
import org.solyton.solawi.bid.module.permission.action.db.IsGranted
import org.solyton.solawi.bid.module.permission.action.db.ReadAvailableRightRoleContexts
import org.solyton.solawi.bid.module.permission.action.db.ReadParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.permission.data.api.Contexts
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.permission.data.api.ReadParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContexts
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUser
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUsers
import org.solyton.solawi.bid.module.permission.data.api.UserToContextsMap

@KtorDsl
fun Routing.permissions(environment: Environment, authenticate: Routing.(Route.() -> Route)-> Route) {
    authenticate {
        route("permissions") {
            route("user") {
                patch("role-right-contexts") {
                    ReceiveContextual<ReadRightRoleContextsOfUser>() *
                    IsGranted(Right.readRightRoleContexts.value){ context ->
                        context.data.userId != context.userId.toString()
                    } *
                    GetRoleRightContexts *
                    Respond<Contexts>() runOn Base(call, environment)
                }
            }

            route("users") {
                patch("role-right-contexts") {
                    ReceiveContextual<ReadRightRoleContextsOfUsers>() *
                    IsGranted(Right.readRightRoleContexts.value) *
                    GetRoleRightContextsOfUsers *
                    Respond<UserToContextsMap>() runOn Base(call, environment)
                }
            }

            route("contexts"){
                patch("parent-child-relations") {
                    ReceiveContextual<ReadParentChildRelationsOfContexts>() *
                    IsGranted(Right.readRightRoleContexts.value) *
                    ReadParentChildRelationsOfContexts *
                    Respond<ParentChildRelationsOfContexts>() runOn Base(call, environment)
                }
                patch("roles-and-rights") {
                    ReceiveContextual<ReadRightRoleContexts>() *
                    IsGranted(Right.readRightRoleContexts.value) *
                    ReadAvailableRightRoleContexts *
                    Respond<Contexts>() runOn Base(call, environment)
                }
            }
        }
    }
}
