package org.solyton.solawi.bid.module.authentication.application

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.evoleq.ktorx.result.map
import org.evoleq.math.state.map
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.util.*
import org.solyton.solawi.bid.application.environment.setupEnvironment
import org.solyton.solawi.bid.application.pipeline.*
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.migrations.authenticationMigrations
import org.solyton.solawi.bid.module.authentication.routing.authentication
import org.solyton.solawi.bid.module.bid.routing.sendBid
import org.solyton.solawi.bid.module.bid.routing.bid
import org.solyton.solawi.bid.module.bid.routing.migrations.bidRoutingMigrations
import org.solyton.solawi.bid.module.user.action.GetUserById
import org.solyton.solawi.bid.module.user.routing.user
import java.util.*


fun Application.authenticationTest() {
    val environment = setupEnvironment()
    installDatabase(environment, authenticationMigrations)

    installDatabase(environment, bidRoutingMigrations)
    installSerializers()
    installAuthentication(environment.jwt)
    installCors()
    installContentNegotiation()
    routing {
        authentication(environment)
        user(environment){ this.it() }
        sendBid(environment)
        bid(environment){
            this.it()
        }
        authenticate ("auth-jwt") {
            get("test-user-principle") {
                (Principle() map { it.map { jwtPrincipal -> UUID.fromString(jwtPrincipal.payload.subject) } }) *
                GetUserById * { result -> { database ->
                    result.map {
                        Login(it.username, "")
                    } to database
                } } *
                Respond() runOn Base(call, environment)
            }
            get("test-receive-contextual") {

                Context() *
                { result -> { database ->
                    result.map { it.userId } to database
                } } *
                GetUserById *
                { result -> { database ->
                    result.map {
                        Login(it.username, "")
                    } to database
                } } *
                Respond() runOn Base(call, environment)
            }
        }
    }

}
