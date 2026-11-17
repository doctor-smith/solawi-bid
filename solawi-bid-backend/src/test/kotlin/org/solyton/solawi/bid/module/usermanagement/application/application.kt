package org.solyton.solawi.bid.module.usermanagement.application

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.*
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.application.environment.setupEnvironment
import org.solyton.solawi.bid.application.pipeline.*
import org.solyton.solawi.bid.module.authentication.migrations.authenticationMigrations
import org.solyton.solawi.bid.module.authentication.routing.authentication
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedInDerivedContext
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedInSpecialContext
import org.solyton.solawi.bid.module.permission.data.api.ContextId
import org.solyton.solawi.bid.module.permission.routing.permissions
import org.solyton.solawi.bid.module.testFramework.provideUserTokens
import org.solyton.solawi.bid.module.testFramework.testContexts
import org.solyton.solawi.bid.module.user.routing.organization
import org.solyton.solawi.bid.module.user.routing.user
import org.solyton.solawi.bid.module.usermanagement.migrations.usermanagementMigrations
import java.util.*

@Serializable
data class PermissionCheckInput(
    override val contextId: String,
    val derivedContextId: String
) : ContextId

fun Application.userManagementTest() {
    val environment = setupEnvironment()
    val transform = environment.transformException
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
            authenticate("auth-jwt") {
                post("permissions/special-context") {
                    (ReceiveContextual { parameters ->
                        PermissionCheckInput(
                            contextId = parameters["special-context"]!!,
                            derivedContextId = parameters["derived-context"]!!
                        )
                    } *
                        IsGrantedInSpecialContext<PermissionCheckInput>("READ_ORGANIZATION") *
                        Transform { _: Contextual<PermissionCheckInput> -> Unit } *
                        Respond<Unit> { transform() }) runOn Base(call, environment)
                }
                post("permissions/derived-context") {
                    (ReceiveContextual { parameters ->
                        PermissionCheckInput(
                            contextId = parameters["special-context"]!!,
                            derivedContextId = parameters["derived-context"]!!
                        )
                    } *
                        IsGrantedInDerivedContext<PermissionCheckInput>(
                            "READ_ORGANIZATION",
                            deriveContextId = { contextual -> UUID.fromString(contextual.data.derivedContextId) }
                        ) *
                        Transform { _: Contextual<PermissionCheckInput> -> Unit } *
                        Respond<Unit> { transform() }) runOn Base(call, environment)
                }
            }
        }
    }
}
