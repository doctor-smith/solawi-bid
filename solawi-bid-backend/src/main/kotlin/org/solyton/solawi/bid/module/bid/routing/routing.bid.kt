package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.ktorx.result.Result


@KtorDsl
@Suppress("UNUSED_PARAMETER")
fun <BidEnv> Routing.bid(
    environment: BidEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BidEnv : KTorEnv, BidEnv: DbEnv =
     authenticate {
        // val transform = environment.transformException
        route("bid") {

            get("all") {
                call.respond(Result.Success("not impl yet!"))
            }

        }
   }
