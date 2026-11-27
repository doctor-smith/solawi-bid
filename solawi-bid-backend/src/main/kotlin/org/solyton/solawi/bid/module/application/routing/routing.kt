package org.solyton.solawi.bid.module.application.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.*
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.solyton.solawi.bid.module.application.action.*
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.application.permission.AppRight
import org.solyton.solawi.bid.module.permission.action.db.IsGranted
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedOneOf
import org.solyton.solawi.bid.module.permission.action.db.isGrantedOneOf
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import java.util.UUID

private val personalApplicationReadRights = setOf(
    "READ_APPLICATION", AppRight.Application.Subscriptions.readAvailableApplications.value
)
private val personalApplicationSubscriptionRights = setOf(AppRight.Application.Subscriptions.subscribeApplications.value)

private fun <T> personalApplicationAccess(rights: Set<String>) = IsGrantedOneOf<T>(rights) { request ->
    val requested = UUID.fromString(request.context)
    val context = ContextEntity.findById(requested)
    if (context?.name == "APPLICATION" && context.root == null && !isGrantedOneOf(request.userId, requested, rights.toList())) {
        // The personal UI enters at APPLICATION. Only its registered platform and enrollment scopes can supply the right.
        ContextEntity.find {
            (ContextsTable.rootId eq context.id) and (ContextsTable.name inList listOf("ORGANIZATION", "MANAGEMENT"))
        }.firstOrNull { isGrantedOneOf(request.userId, it.id.value, rights.toList()) }?.id?.value ?: requested
    } else requested
}

fun <ApplicationEnv> Routing.application(
    environment: ApplicationEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where ApplicationEnv : KTorEnv, ApplicationEnv : DbEnv {
    val transform = environment.transformException
    authenticate{
        route("applications"){
            get("all") {
                Receive(ReadApplications) *
                ReadAllApplications() *
                Respond<ApiApplications>{ transform() } runOn Base(call, environment)
            }
            route("personal") {
                get("all") {
                    ReceiveContextual(ReadPersonalUserApplications) *
                    personalApplicationAccess(personalApplicationReadRights) *
                    ReadPersonalUserApplications() *
                    Respond<ApiApplications>{ transform() } runOn Base(call, environment)
                }

                get("application-context-relations") {
                    ReceiveContextual(ReadPersonalApplicationContextRelations) *
                    personalApplicationAccess(personalApplicationReadRights) *
                    ReadPersonalApplicationContextRelations() *
                    Respond< ApplicationContextRelations>{ transform() } runOn Base(call, environment)
                }
                patch("register") {
                    ReceiveContextual<RegisterForApplications>() *
                    personalApplicationAccess(personalApplicationSubscriptionRights) *
                    RegisterForApplications() *
                    Respond<ApiApplications> { transform() } runOn Base(call, environment)
                }
                patch("trial") {
                    ReceiveContextual< StartTrialsOfApplications>() *
                    personalApplicationAccess(personalApplicationSubscriptionRights) *
                    StartTrialsOfApplications() *
                    Respond<ApiApplications> { transform() } runOn Base(call, environment)
                }
                patch("subscribe") {
                    ReceiveContextual<SubscribeApplications>() *
                    personalApplicationAccess(personalApplicationSubscriptionRights) *
                    SubscribeApplications() *
                    Respond<ApiApplications> { transform() } runOn Base(call, environment)
                }
                get("organization-context-relations") {
                    ReceiveContextual(ReadApplicationOrganizationContextRelations) *
                    ReadApplicationOrganizationContextRelations() *
                    Respond<ApplicationOrganizationRelations>{ transform() } runOn Base(call, environment)
                }
                // End point can only be used as owner of the application
                // which is to be connected to the organization
                post("connect-organization") {
                    ReceiveContextual<ConnectApplicationToOrganization>() *
                    // Permissions / Ownership is checked during the following step !!
                    // Background:
                    // if the current user has not purchased the application,
                    // an exception will be thrown
                    ConnectApplicationToOrganization() *
                    Respond<ApplicationOrganizationRelations>{ transform() } runOn Base(call, environment)
                }
                patch("update-organization-module-relations") {
                    NotImplemented() * Respond { transform() } runOn Base(call, environment)
                }
            }
            route("modules") {
                route("personal") {
                    get("module-context-relations") {
                        ReceiveContextual(ReadPersonalModuleContextRelations) *
                        personalApplicationAccess(personalApplicationReadRights) *
                        ReadPersonalModuleContextRelations() *
                        Respond<ModuleContextRelations>{ transform() } runOn Base(call, environment)
                    }
                    patch("register") {
                        ReceiveContextual<RegisterForModules>() *
                        personalApplicationAccess(personalApplicationSubscriptionRights) *
                        RegisterForModules() *
                        Respond<ApiApplications>{ transform() } runOn Base(call, environment)
                    }
                    patch("trial") {
                        ReceiveContextual<StartTrialsOfModules>() *
                        personalApplicationAccess(personalApplicationSubscriptionRights) *
                        StartTrialsOfModules() *
                        Respond<ApiApplications>{ transform() } runOn Base(call, environment)
                    }
                    patch("subscribe") {
                        ReceiveContextual<SubscribeModules>() *
                        personalApplicationAccess(personalApplicationSubscriptionRights) *
                        SubscribeModules() *
                        Respond<ApiApplications>{ transform() } runOn Base(call, environment)
                    }
                }
            }
            route("management") {
                patch("users") {
                    ReceiveContextual<ReadUserApplications>() *
                    IsGranted("MANAGE_ACCESS_TO_APPS") *
                    ReadApplicationsOfUsers() *
                    Respond<ApiUserApplications> { transform() } runOn Base(call, environment)
                }
                route("tech") {
                    patch("update-standard-application-context") {
                        ReceiveContextual<UpdateStandardApplicationContext>() *
                        IsGranted("UPDATE_APPLICATION") *
                        UpdateStandardApplicationContext() *
                        Respond<Context> { transform() } runOn Base(call, environment)
                    }
                    patch("fix-application-related-contexts") {
                        ReceiveContextual<FixApplicationRelatedContexts>() *
                        IsGranted("UPDATE_APPLICATION") *
                        FixApplicationRelatedContexts() *
                        Respond<Unit> { transform() } runOn Base(call, environment)
                    }
                    patch("update-standard-module-context") {
                        NotImplemented("Updates of standard module contexts is no implemented yet") * Respond<Unit> { transform() } runOn Base(call, environment)
                    }
                    patch("fix-module-related-contextas") {
                        ReceiveContextual<FixModuleRelatedContexts>() *
                        IsGranted("UPDATE_APPLICATION") *
                        FixModuleRelatedContexts() *
                        Respond<Unit> { transform() } runOn Base(call, environment)
                    }
                }
            }
        }
    }
}
