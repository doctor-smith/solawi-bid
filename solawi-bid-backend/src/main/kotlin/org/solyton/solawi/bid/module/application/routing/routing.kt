package org.solyton.solawi.bid.module.application.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.Receive
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.application.action.ReadAllApplications
import org.solyton.solawi.bid.module.application.action.ReadApplicationsOfUsers
import org.solyton.solawi.bid.module.application.action.ReadPersonalUserApplications
import org.solyton.solawi.bid.module.application.action.RegisterForApplications
import org.solyton.solawi.bid.module.application.action.StartTrialsOfApplications
import org.solyton.solawi.bid.module.application.action.SubscribeApplications
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ApiUserApplications
import org.solyton.solawi.bid.module.application.data.ReadApplications
import org.solyton.solawi.bid.module.application.data.ReadPersonalUserApplications
import org.solyton.solawi.bid.module.application.data.ReadUserApplications
import org.solyton.solawi.bid.module.application.data.RegisterForApplications
import org.solyton.solawi.bid.module.application.data.StartTrialsOfApplications
import org.solyton.solawi.bid.module.application.data.SubscribeApplications
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
            route("personal")             {
                get("") {
                    ReceiveContextual(ReadPersonalUserApplications) *
                    IsGranted("READ_APPLICATION") *
                    ReadPersonalUserApplications() *
                    Respond<ApiApplications>{ transform() } runOn Base(call, environment)
                }
                patch("register") {
                    ReceiveContextual<RegisterForApplications>() *
                    IsGranted("SUBSCRIBE_APPLICATION") *
                    RegisterForApplications() *
                    Respond<ApiApplications> { transform() } runOn Base(call, environment)
                }
                patch("trial") {
                    ReceiveContextual< StartTrialsOfApplications>() *
                    IsGranted("SUBSCRIBE_APPLICATION") *
                    StartTrialsOfApplications() *
                    Respond<ApiApplications> { transform() } runOn Base(call, environment)
                }
                patch("subscribe") {
                    ReceiveContextual<SubscribeApplications>() *
                    IsGranted("SUBSCRIBE_APPLICATION") *
                    SubscribeApplications() *
                    Respond<ApiApplications> { transform() } runOn Base(call, environment)
                }
            }
            patch("modules/subscribe") {
                TODO("Not implemented")
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
