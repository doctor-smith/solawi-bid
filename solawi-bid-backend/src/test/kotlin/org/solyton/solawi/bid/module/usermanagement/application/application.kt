package org.solyton.solawi.bid.module.usermanagement.application

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.solyton.solawi.bid.application.environment.setupEnvironment
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.application.pipeline.installAuthentication
import org.solyton.solawi.bid.application.pipeline.installContentNegotiation
import org.solyton.solawi.bid.application.pipeline.installCors
import org.solyton.solawi.bid.application.pipeline.installDatabase
import org.solyton.solawi.bid.application.pipeline.installSerializers
import org.solyton.solawi.bid.module.application.migrations.applicationMigrations
import org.solyton.solawi.bid.module.application.routing.application
import org.solyton.solawi.bid.module.application.schema.ApplicationContextsTable
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModuleContextsTable
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.authentication.migrations.authenticationMigrations
import org.solyton.solawi.bid.module.authentication.routing.authentication
import org.solyton.solawi.bid.module.permission.data.api.ApiContext
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.permission.routing.permissions
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.testFramework.provideUserTokens
import org.solyton.solawi.bid.module.testFramework.testContexts
import org.solyton.solawi.bid.module.user.data.api.ApiUser
import org.solyton.solawi.bid.module.user.data.api.ApiUsers
import org.solyton.solawi.bid.module.user.routing.organization
import org.solyton.solawi.bid.module.user.routing.user
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable
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
