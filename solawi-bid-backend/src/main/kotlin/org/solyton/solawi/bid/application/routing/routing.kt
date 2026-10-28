package org.solyton.solawi.bid.application.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
            call.respondText(
                json,
                ContentType.Application.Json
            )
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
