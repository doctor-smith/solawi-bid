package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.ktorx.result.Result
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.util.Base
import org.evoleq.util.Receive
import org.evoleq.util.Respond
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.module.bid.action.db.CreateAuction
import org.solyton.solawi.bid.module.bid.action.db.ReadAuctions
import org.solyton.solawi.bid.module.bid.action.db.DeleteAuctions
import org.solyton.solawi.bid.module.bid.action.db.StoreBid
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.api.GetAuctions
import org.solyton.solawi.bid.module.bid.data.api.CreateAuction
import org.solyton.solawi.bid.module.bid.data.api.DeleteAuctions


@KtorDsl
fun Routing.bid(environment: Environment,authenticate: Routing.(Route.() -> Route)-> Route) =
    authenticate {
        route("bid") {

            get("all") {
                call.respond(Result.Success("not impl yet!"))
                // getAllUsers() runOn Base(call, environment)

            }

            post("send") {
                // send bid
                // params: Crypto Link, bid-amount
                //
                (Receive<Bid>() * StoreBid * Respond()) runOn Base(call, environment)
            }
        }
    }
@KtorDsl
fun Routing.auction(environment: Environment,authenticate: Routing.(Route.() -> Route)-> Route) =
    authenticate{
        route("auction"){
            post("create") {
                (Receive<CreateAuction>() * CreateAuction * Respond()) runOn Base(call, environment)
            }
            patch("update") {

            }
            delete("delete") {
                (Receive<DeleteAuctions>() * DeleteAuctions * ReadAuctions * Respond()) runOn Base(call, environment)
            }
            get("results") {

            }
            get("all"){
                (Receive(GetAuctions) * ReadAuctions * Respond()) runOn Base(call, environment)
            }
        }
    }

@KtorDsl
fun Routing.round(environment: Environment) = route("round") {
    post("create") {

    }
    patch("start") {  }
    patch("stop") {  }
    patch("freeze") {  }

}