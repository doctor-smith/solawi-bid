package org.solyton.solawi.bid.module.banking.routing

import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.NotImplemented
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.math.state.runOn
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.banking.action.CreateFiscalYear
import org.solyton.solawi.bid.module.banking.action.ReadFiscalYearsByProvider
import org.solyton.solawi.bid.module.banking.action.UpdateFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.CreateFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.UpdateFiscalYear
import org.solyton.solawi.bid.module.permission.action.db.IsGranted

fun <BankingEnv> Routing.banking (
    environment: BankingEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BankingEnv : KTorEnv, BankingEnv : DbEnv {
    val transform = environment.transformException
    authenticate {
        route("banking") {
            route("fiscal-years") {
                get("all") {
                    val legalEntityId = call.parameters["legal-entity"]
                    ReceiveContextual(legalEntityId) *
                    IsGranted("READ_FISCAL_YEARS") *
                    ReadFiscalYearsByProvider() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    ReceiveContextual<CreateFiscalYear>() *
                    IsGranted("CREATE_FISCAL_YEAR") *
                    CreateFiscalYear() *
                    Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    ReceiveContextual<UpdateFiscalYear>() *
                    IsGranted("UPDATE_FISCAL_YEAR") *
                    UpdateFiscalYear() *
                    Respond{ transform() } runOn Base(call, environment)
                }
            }
            route("accounts") {
                get("all"){
                    NotImplemented() * Respond{ transform() } runOn Base(call, environment)
                }
                post("create") {
                    NotImplemented() * Respond{ transform() } runOn Base(call, environment)
                }
                patch("update") {
                    NotImplemented() * Respond{ transform() } runOn Base(call, environment)
                }
            }
        }
    }
}
