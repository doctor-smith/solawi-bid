package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.*
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.ktorx.result.Result
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.bid.action.db.*
import org.solyton.solawi.bid.module.bid.data.api.*

@KtorDsl
fun <BidEnv> Routing.sendBid(
    environment: BidEnv
) where BidEnv : KTorEnv, BidEnv: DbEnv =  route("bid") {
    val transform = environment.transformException
     post("send") {
        (Receive<Bid>() * StoreBid * Respond<BidRound>{ transform() }) runOn Base(call, environment)
    }
}


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


@KtorDsl
fun <BidEnv> Routing.auction(
    environment: BidEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
)  where BidEnv : KTorEnv, BidEnv: DbEnv=
    authenticate{
        val transform = environment.transformException

        route("auction"){
            post("create") {
                ReceiveContextual<CreateAuction>() * CreateAuction * Respond<Auction>{ transform() } runOn Base(call, environment)
            }
            patch("update") {
                (ReceiveContextual<UpdateAuctions>() * UpdateAuctions * ReadAuctions * Respond<Auctions>{ transform() }) runOn Base(call, environment)
            }
            patch("configure") {
                ReceiveContextual<ConfigureAuction>() * ConfigureAuction * Respond<Auction>{ transform() } runOn Base(call, environment)
            }
            delete("delete") {
                (ReceiveContextual<DeleteAuctions>() * DeleteAuctions * ReadAuctions * Respond<Auctions>{ transform() }) runOn Base(call, environment)
            }

            get("results") {

            }
            patch("accept-round") {
                (Receive<AcceptRound>()) * AcceptRound * Respond<AcceptedRound>{ transform() } runOn Base(call, environment)
            }
            get("all"){
                (Receive(GetAuctions) * ReadAuctions * Respond<Auctions>{ transform() }) runOn Base(call, environment)
            }
            route("bidder") {
                post("import") {
                    (Receive<ImportBidders>() *
                    ImportBidders *
                    Respond<Auction>{ transform() }) runOn Base(call, environment)
                }
                delete("delete"){
                    // will delete all listed bidders
                    (Receive<DeleteBidders>() * Fail("Not Implemented") * Respond{ transform() }) runOn Base(call, environment)
                }
            }

        }
    }

@KtorDsl
fun <BidEnv> Routing.round(
    environment: BidEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BidEnv : KTorEnv, BidEnv: DbEnv =
    authenticate{
        val transform = environment.transformException
        route("round") {
            post("create") {
                Receive<CreateRound>() * CreateRound * Respond{ transform() }runOn Base(call,environment)
            }
            patch("change-state") {
                Receive<ChangeRoundState>() * ChangeRoundState *  Respond{ transform() } runOn Base(call,environment)
            }

            patch("export-results") {
                Receive<ExportBidRound>() * ExportResults * Respond{ transform() } runOn Base(call, environment)
            }

            patch("evaluate") {
                Receive<EvaluateBidRound>() * EvaluateBidRound * Respond{ transform() } runOn Base(call, environment)
            }

            patch("pre-evaluate") {
                Receive<PreEvaluateBidRound>() * PreEvaluateBidRound * Respond{ transform() } runOn Base(call, environment)
            }

            post("add-comment") {
                ReceiveContextual<CommentOnRound>() * CommentOnRound * Respond { transform() } runOn Base(call, environment)
            }
        }
    }

@KtorDsl
fun <BidEnv> Routing.bidders(
    environment: BidEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BidEnv : KTorEnv, BidEnv: DbEnv =
    authenticate{
        val transform = environment.transformException
        route("bidders") {
            patch("search") {
                Receive<SearchBidderData>() * SearchBidderMails * Respond<BidderMails>{ transform() } runOn Base(call, environment)
            }
            post("add") {
                Receive<AddBidders>() * AddBidders * Respond<Unit>{ transform() } runOn Base(call, environment)
            }

        }
    }
