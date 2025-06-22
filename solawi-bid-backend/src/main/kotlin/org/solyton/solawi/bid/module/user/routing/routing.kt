package org.solyton.solawi.bid.module.user.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.Receive
import org.solyton.solawi.bid.application.action.io.Respond
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.module.user.action.ChangePassword
import org.solyton.solawi.bid.module.user.action.CreateNewUser
import org.solyton.solawi.bid.module.user.action.GetAllUsers
import org.solyton.solawi.bid.module.user.data.api.*

@KtorDsl
fun Routing.user(environment: Environment, authenticate: Routing.(Route.() -> Route)-> Route) {
    authenticate {
        route("users") {
            get("all") {

                // val principal = call.authentication.principal<JWTPrincipal>()
                // val userId = principal?.payload?.subject ?: "Unknown"
                Receive(GetUsers) * GetAllUsers * Respond<Users>() runOn Base(call, environment)
            }

            post("create") {
                Receive<CreateUser>() * CreateNewUser * Respond<User>() runOn Base(call, environment)
            }

            patch("change-password") {
                Receive<ChangePassword>() * ChangePassword * Respond<User>() runOn Base(call, environment)
            }
        }
    }
}
