package org.solyton.solawi.bid.application.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.Receive
import org.evoleq.ktorx.Respond
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.application.action.io.transform
import org.solyton.solawi.bid.application.data.BuildInfo
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.module.application.routing.application
import org.solyton.solawi.bid.module.authentication.routing.authentication
import org.solyton.solawi.bid.module.banking.routing.banking
import org.solyton.solawi.bid.module.bid.routing.auction
import org.solyton.solawi.bid.module.bid.routing.bidders
import org.solyton.solawi.bid.module.bid.routing.round
import org.solyton.solawi.bid.module.bid.routing.sendBid
import org.solyton.solawi.bid.module.distribution.routing.distributionPoints
import org.solyton.solawi.bid.module.health.routing.health
import org.solyton.solawi.bid.module.permission.routing.permissions
import org.solyton.solawi.bid.module.shares.routing.shares
import org.solyton.solawi.bid.module.user.routing.organization
import org.solyton.solawi.bid.module.user.routing.user
import java.io.File

fun Application.setupRouting(environment: Environment) {
    routing {
        get("/api/version") {
            val json = File("/app/version.json").readText()
            val buildInfo = Json.decodeFromString<BuildInfo>(json)
            Receive<BuildInfo>(buildInfo) *
            Respond<BuildInfo> { transform() } runOn Base(call, environment)
        }
        authentication(environment)
        health(environment)
        user(environment){
            authenticate("auth-jwt"){ it() }
        }
        organization(environment){
            authenticate("auth-jwt"){ it() }
        }
        permissions(environment){
            authenticate("auth-jwt"){ it() }
        }
        sendBid(environment)
        auction(environment){
            authenticate("auth-jwt"){ it() }
        }
        round(environment){
            authenticate("auth-jwt"){ it() }
        }
        bidders(environment){
            authenticate("auth-jwt"){ it() }
        }
        distributionPoints(environment) {
            authenticate("auth-jwt"){ it() }
        }
        shares(environment) {
            authenticate("auth-jwt"){ it() }
        }
        application(environment){
            authenticate("auth-jwt"){ it() }
        }
        banking(environment) {
            authenticate("auth-jwt"){ it() }
        }
    }
}
