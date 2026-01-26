package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.call
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.NotImplemented
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times

@KtorDsl
fun <SharesEnv> Routing.shares(
    environment: SharesEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where SharesEnv : KTorEnv, SharesEnv: DbEnv =
authenticate {
    val transform = environment.transformException
    route("shares") {
        get() {
            NotImplemented() * Respond<Unit> { transform() } runOn Base(call, environment)
        }
    }
}
