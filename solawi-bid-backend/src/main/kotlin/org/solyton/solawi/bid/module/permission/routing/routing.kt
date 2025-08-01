package org.solyton.solawi.bid.module.permission.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.solyton.solawi.bid.module.permission.permission.Right
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
fun <PermissionEnv> Routing.permissions(
    environment: PermissionEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where PermissionEnv: KTorEnv, PermissionEnv: DbEnv {
    val transform = environment.transformException
    authenticate {
        route("permissions") {
            route("user") {
                patch("role-right-contexts") {
                    ReceiveContextual<ReadRightRoleContextsOfUser>() *
                    IsGranted(Right.ReadRightRoleContexts.value){ context ->
                        context.data.userId != context.userId.toString()
                    } *
                    GetRoleRightContexts *
                    Respond<Contexts> { transform() } runOn Base(call, environment)
                }
            }

            route("users") {
                patch("role-right-contexts") {
                    ReceiveContextual<ReadRightRoleContextsOfUsers>() *
                    IsGranted(Right.ReadRightRoleContexts.value) *
                    GetRoleRightContextsOfUsers *
                    Respond<UserToContextsMap>{ transform() } runOn Base(call, environment)
                }
            }

            route("contexts"){
                patch("parent-child-relations") {
                    ReceiveContextual<ReadParentChildRelationsOfContexts>() *
                    IsGranted(Right.ReadRightRoleContexts.value) *
                    ReadParentChildRelationsOfContexts *
                    Respond<ParentChildRelationsOfContexts>{ transform() } runOn Base(call, environment)
                }
                patch("roles-and-rights") {
                    ReceiveContextual<ReadRightRoleContexts>() *
                    IsGranted(Right.ReadRightRoleContexts.value) *
                    ReadAvailableRightRoleContexts *
                    Respond<Contexts>{ transform() } runOn Base(call, environment)
                }
            }
        }
    }
}
