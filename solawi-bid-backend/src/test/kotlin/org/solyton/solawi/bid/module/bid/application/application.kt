package org.solyton.solawi.bid.module.bid.application

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.solyton.solawi.bid.application.environment.setupEnvironment
import org.solyton.solawi.bid.application.pipeline.*
import org.solyton.solawi.bid.module.application.routing.application
import org.solyton.solawi.bid.module.authentication.migrations.authenticationMigrations
import org.solyton.solawi.bid.module.bid.routing.*
import org.solyton.solawi.bid.module.bid.routing.migrations.bidRoutingMigrations
import org.solyton.solawi.bid.module.testFramework.provideUserTokens
import org.solyton.solawi.bid.module.testFramework.testContexts


fun Application.bidTest() {
    val environment = setupEnvironment()
    installDatabase(environment, bidRoutingMigrations)
    installDatabase(environment, authenticationMigrations)
    installAuthentication(environment.jwt)
    installSerializers()
    installCors()
    installContentNegotiation()
    routing {
        val database = environment.connectToDatabase()
        provideUserTokens(environment.jwt, database)
        testContexts(database)
        application(environment) {
            authenticate("auth-jwt"){ it() }
        }
        sendBid(environment)
        bid(environment){
            this.it()
        }
        auction(environment) {
            authenticate("auth-jwt"){ it() }
            // this.it()
        }
        round(environment){
            authenticate("auth-jwt"){ it() }
            // this.it()
        }
        bidders(environment){
            authenticate("auth-jwt"){ it() }
            // this.it()
        }
    }
}
