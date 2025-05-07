package org.solyton.solawi.bid.module.authentication.routing

// import org.solyton.solawi.bid.application.plugin.AuthenticationHolder.Companion.jwtPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.state.bind
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.util.*
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.module.authentication.action.*
import org.solyton.solawi.bid.module.authentication.data.api.LoggedInAs
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.authentication.data.api.RefreshToken

fun Routing.authentication(environment: Environment) {

    patch("/is-logged-in") {
        IsLoggedIn(jwt = environment.jwt) * Respond<LoggedInAs>() runOn Base(call, environment)
    }

    // Login endpoint
    post("/login") {
        Receive<Login>() * Login(jwt = environment.jwt) * Respond() runOn Base(call, environment)

    }

    // Refresh token endpoint
    post("/refresh") {
        Receive<RefreshToken>() * Refresh(jwt = environment.jwt) * Respond() runOn Base(call, environment)
    }

    // Logout endpoint
    patch("/logout") {
        Receive<Logout>() * LogoutUser() * Respond() runOn Base(call, environment)
        /*
        val refreshToken = call.receive<Logout>().refreshToken
        if (refreshToken != null) {
            revokeRefreshToken(refreshToken)
            call.respond(HttpStatusCode.OK, "Logged out successfully")
        } else {
            call.respond(HttpStatusCode.BadRequest, "Refresh token is required for logout")
        }

         */
    }
}
