package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.ktorx.result.Result
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.util.*
import org.solyton.solawi.bid.application.environment.Environment
import org.solyton.solawi.bid.module.bid.action.db.*
import org.solyton.solawi.bid.module.bid.data.api.*


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
                (Receive<CreateAuction>() * CreateAuction * Respond<Auction>()) runOn Base(call, environment)
            }
            patch("update") {
                (Receive<UpdateAuctions>() * UpdateAuctions * ReadAuctions * Respond<Auctions>()) runOn Base(call, environment)
            }
            delete("delete") {
                (Receive<DeleteAuctions>() * DeleteAuctions * ReadAuctions * Respond<Auctions>()) runOn Base(call, environment)
            }

            get("results") {

            }
            get("all"){
                (Receive(GetAuctions) * ReadAuctions * Respond<Auctions>()) runOn Base(call, environment)
            }
            route("bidder") {
                post("import") {
                    (Receive<ImportBidders>() * ImportBidders * Respond<Auction>()) runOn Base(call, environment)
                }
                delete("delete"){
                    // will delete all listed bidders
                    (Receive<DeleteBidders>() * Fail("Not Implemented") * Respond()) runOn Base(call, environment)
                }
            }

        }
    }

@KtorDsl
fun Routing.round(environment: Environment) = route("round") {
    post("create") {
        // here you have to return the complete link
        // baseUrlOfFrontend/cryptoStuff
    }
    patch("start") {  }
    patch("stop") {  }
    patch("freeze") {  }

}