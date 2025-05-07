package org.solyton.solawi.bid.application.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.module.authentication.routing.authentication
import org.solyton.solawi.bid.module.bid.routing.auction
import org.solyton.solawi.bid.module.bid.routing.bidders
import org.solyton.solawi.bid.module.bid.routing.round
import org.solyton.solawi.bid.module.bid.routing.sendBid
import org.solyton.solawi.bid.module.health.routing.health
import org.solyton.solawi.bid.module.user.routing.user

fun Application.setupRouting(environment: Environment) {
    routing {
        authentication(environment)
        health(environment)
        user(environment){
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
    }
}
