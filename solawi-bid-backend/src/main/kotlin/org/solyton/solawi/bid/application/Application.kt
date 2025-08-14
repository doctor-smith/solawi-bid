package org.solyton.solawi.bid.application


import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import org.slf4j.event.Level
import org.solyton.solawi.bid.application.data.db.migrations.applicationMigrations
import org.solyton.solawi.bid.application.environment.setupEnvironment
import org.solyton.solawi.bid.application.pipeline.*
import org.solyton.solawi.bid.application.routing.setupRouting

fun Application.solawiBid(test: Boolean = false) {
        val environment = setupEnvironment()
        if(!test) {
            val database = installDatabase(environment, applicationMigrations)
            installUsers(environment, database)
        }
        installSerializers()
        installAuthentication(environment.jwt)
        install(DefaultHeaders) {
        }
        install(CallLogging) {
                level = Level.INFO
        }
        install(ForwardedHeaders)
        install(XForwardedHeaders)
        installCors()
        installContentNegotiation()
        setupRouting(environment)

        interceptAndValidateHeaders()
}
