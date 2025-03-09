package org.solyton.solawi.bid.module.user.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.util.Base
import org.evoleq.util.Receive
import org.evoleq.util.Respond
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.module.user.action.CreateNewUser
import org.solyton.solawi.bid.module.user.action.GetAllUsers
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.GetUsers
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.data.api.Users

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
        }
    }
}
