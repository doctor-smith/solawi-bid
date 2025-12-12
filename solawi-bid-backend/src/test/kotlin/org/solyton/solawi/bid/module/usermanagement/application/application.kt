package org.solyton.solawi.bid.module.usermanagement.application

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.solyton.solawi.bid.application.environment.setupEnvironment
import org.solyton.solawi.bid.application.pipeline.installAuthentication
import org.solyton.solawi.bid.application.pipeline.installContentNegotiation
import org.solyton.solawi.bid.application.pipeline.installCors
import org.solyton.solawi.bid.application.pipeline.installDatabase
import org.solyton.solawi.bid.application.pipeline.installSerializers
import org.solyton.solawi.bid.module.authentication.migrations.authenticationMigrations
import org.solyton.solawi.bid.module.authentication.routing.authentication
import org.solyton.solawi.bid.module.permission.routing.permissions
import org.solyton.solawi.bid.module.testFramework.provideUserTokens
import org.solyton.solawi.bid.module.testFramework.testContexts
import org.solyton.solawi.bid.module.user.routing.organization
import org.solyton.solawi.bid.module.user.routing.user
import org.solyton.solawi.bid.module.usermanagement.migrations.usermanagementMigrations

fun Application.userManagementTest() {
    val environment = setupEnvironment()
    installDatabase(environment, authenticationMigrations)
    installDatabase(environment, usermanagementMigrations)
    installAuthentication(environment.jwt)
    installCors()
    installContentNegotiation()
    installSerializers()
    routing {
        // real modules
        authentication(environment)
        user(environment) { authenticate("auth-jwt"){ it() } }
        organization(environment) { authenticate("auth-jwt"){ it() } }
        permissions(environment) { authenticate("auth-jwt"){ it() }}

        // helper modules
        val database = environment.connectToDatabase()
        provideUserTokens(environment.jwt, database)
        testContexts(database)
        route("setup") {

        }
    }
}
