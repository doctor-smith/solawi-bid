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
import org.solyton.solawi.bid.module.application.action.ReadPersonalUserApplications
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ReadApplications
import org.solyton.solawi.bid.module.application.data.ReadPersonalUserApplications

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
            get("personal") {
                ReceiveContextual(ReadPersonalUserApplications) *
                        ReadPersonalUserApplications() *
                        Respond<ApiApplications>{ transform() } runOn Base(call, environment)
            }
            patch("subscribe") {

            }
            patch("modules/subscribe") {

            }
            route("management") {
                patch("users") {
                    TODO("not implemented yet")
                }
            }
        }
    }
}
