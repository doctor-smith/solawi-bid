package org.solyton.solawi.bid.module.permission

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.util.Base
import org.evoleq.util.ReceiveContextual
import org.evoleq.util.Respond
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.module.permission.action.db.GetRoleRightContexts
import org.solyton.solawi.bid.module.permission.action.db.GetRoleRightContextsOfUsers
import org.solyton.solawi.bid.module.permission.action.db.IsGranted
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUser
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUsers

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
                    Respond<List<Context>>() runOn Base(call, environment)
                }
            }

            route("users") {
                patch("right-role-contexts") {
                    ReceiveContextual<ReadRightRoleContextsOfUsers>() *
                        IsGranted(Right.readRightRoleContexts.value) *
                        GetRoleRightContextsOfUsers *
                        Respond<Map<String,List<Context>>>() runOn Base(call, environment)
                }
            }
        }
    }
}