package org.solyton.solawi.bid.module.bid.application

import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import org.solyton.solawi.bid.application.environment.setupEnvironment
import org.solyton.solawi.bid.application.pipeline.installAuthentication
import org.solyton.solawi.bid.application.pipeline.installContentNegotiation
import org.solyton.solawi.bid.application.pipeline.installCors
import org.solyton.solawi.bid.application.pipeline.installDatabase
import org.solyton.solawi.bid.application.pipeline.installSerializers
import org.solyton.solawi.bid.module.authentication.migrations.authenticationMigrations
import org.solyton.solawi.bid.module.bid.routing.*
import org.solyton.solawi.bid.module.bid.routing.migrations.bidRoutingMigrations
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
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
