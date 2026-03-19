package org.solyton.solawi.bid.module.application.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.*
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.application.action.*
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.permission.action.db.IsGranted

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
                    IsGranted("READ_APPLICATION") *
                    ReadPersonalUserApplications() *
                    Respond<ApiApplications>{ transform() } runOn Base(call, environment)
                }

                get("application-context-relations") {
                    ReceiveContextual(ReadPersonalApplicationContextRelations) *
                    IsGranted("READ_APPLICATION") *
                    ReadPersonalApplicationContextRelations() *
                    Respond< ApplicationContextRelations>{ transform() } runOn Base(call, environment)
                }
                patch("register") {
                    ReceiveContextual<RegisterForApplications>() *
                    IsGranted("SUBSCRIBE_APPLICATIONS") *
                    RegisterForApplications() *
                    Respond<ApiApplications> { transform() } runOn Base(call, environment)
                }
                patch("trial") {
                    ReceiveContextual< StartTrialsOfApplications>() *
                    IsGranted("SUBSCRIBE_APPLICATIONS") *
                    StartTrialsOfApplications() *
                    Respond<ApiApplications> { transform() } runOn Base(call, environment)
                }
                patch("subscribe") {
                    ReceiveContextual<SubscribeApplications>() *
                    IsGranted("SUBSCRIBE_APPLICATIONS") *
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
                        IsGranted("READ_APPLICATION") *
                        ReadPersonalModuleContextRelations() *
                        Respond<ModuleContextRelations>{ transform() } runOn Base(call, environment)
                    }
                    patch("register") {
                        ReceiveContextual<RegisterForModules>() *
                        IsGranted("SUBSCRIBE_APPLICATIONS") *
                        RegisterForModules() *
                        Respond<ApiApplications>{ transform() } runOn Base(call, environment)
                    }
                    patch("trial") {
                        ReceiveContextual<StartTrialsOfModules>() *
                        IsGranted("SUBSCRIBE_APPLICATIONS") *
                        StartTrialsOfModules() *
                        Respond<ApiApplications>{ transform() } runOn Base(call, environment)
                    }
                    patch("subscribe") {
                        ReceiveContextual<SubscribeModules>() *
                        IsGranted("SUBSCRIBE_APPLICATIONS") *
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
            }
        }
    }
}
