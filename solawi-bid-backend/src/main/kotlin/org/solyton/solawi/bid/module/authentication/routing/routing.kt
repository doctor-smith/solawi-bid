package org.solyton.solawi.bid.module.authentication.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.Receive
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.authentication.action.IsLoggedIn
import org.solyton.solawi.bid.module.authentication.action.Login
import org.solyton.solawi.bid.module.authentication.action.LogoutUser
import org.solyton.solawi.bid.module.authentication.action.Refresh
import org.solyton.solawi.bid.module.authentication.data.api.LoggedInAs
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.authentication.data.api.RefreshToken
import org.solyton.solawi.bid.module.authentication.environment.JwtEnv

fun <AuthEnv> Routing.authentication(
    environment: AuthEnv
) where AuthEnv: KTorEnv, AuthEnv: DbEnv, AuthEnv : JwtEnv {

    val transform = environment.transformException

    patch("/is-logged-in") {
        IsLoggedIn(jwt = environment.jwt) * Respond<LoggedInAs>{ transform()  } runOn Base(call, environment)
    }

    // Login endpoint
    post("/login") {
        Receive<Login>() * Login(jwt = environment.jwt) * Respond{ transform() } runOn Base(call, environment)

    }

    // Refresh token endpoint
    post("/refresh") {
        Receive<RefreshToken>() * Refresh(jwt = environment.jwt) * Respond{ transform() } runOn Base(call, environment)
    }

    // Logout endpoint
    patch("/logout") {
        Receive<Logout>() * LogoutUser() * Respond{ transform() } runOn Base(call, environment)
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
