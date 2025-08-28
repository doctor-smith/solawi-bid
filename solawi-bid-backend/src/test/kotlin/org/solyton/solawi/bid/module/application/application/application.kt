package org.solyton.solawi.bid.module.application.application

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
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
import org.solyton.solawi.bid.module.authentication.migrations.authenticationMigrations
import org.solyton.solawi.bid.module.authentication.routing.authentication
import org.solyton.solawi.bid.module.permission.data.api.ApiContext
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable

fun Application.applicationTest() {
    val environment = setupEnvironment()
    installDatabase(environment, authenticationMigrations)
    installDatabase(environment, applicationMigrations)
    installAuthentication(environment.jwt)
    installCors()
    installContentNegotiation()
    installSerializers()
    routing {
        authentication(environment)
        application(environment) {
            authenticate("auth-jwt"){ it() }
        }

        val database = environment.connectToDatabase()
        route("setup") {
            get("root-context-by-name") {
                val contextName = call.request.headers[Header.CONTEXT]!!
                val contextEntity: ContextEntity = transaction(database) {
                    ContextEntity.find {
                        ContextsTable.name eq contextName and (ContextsTable.rootId eq null)
                    }.firstOrNull() ?: throw ContextException.NoSuchContext(contextName)
                }
                call.respond(
                    ApiContext(
                        id = contextEntity.id.value.toString(),
                        name = contextName,
                        roles = listOf()
                    )
                )
            }
        }
    }
}
