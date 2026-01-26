package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.Fail
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.bid.action.db.*
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.permission.action.db.IsGranted
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedInSpecialContext

@KtorDsl
fun <BidEnv> Routing.auction(
    environment: BidEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
)  where BidEnv : KTorEnv, BidEnv: DbEnv=
    authenticate{
        val transform = environment.transformException

        route("auction"){
            post("create") {
                ReceiveContextual<CreateAuction>() *
                        IsGrantedInSpecialContext("CREATE_AUCTION") *
                        CreateAuction *
                        Respond<Auction>{ transform() } runOn
                        Base(call, environment)
            }
            patch("update") {
                (ReceiveContextual<UpdateAuctions>() *
                        IsGranted("UPDATE_AUCTION",) *
                        UpdateAuctions *
                        ReadAllAuctions *
                        Respond<Auctions>{ transform() }
                        ) runOn Base(call, environment)

            }
            patch("configure") {
                ReceiveContextual<ConfigureAuction>() * ConfigureAuction * Respond<Auction>{ transform() } runOn Base(call, environment)
            }
            delete("delete") {
                (ReceiveContextual<DeleteAuctions>() * DeleteAuctions * ReadAllAuctions * Respond<Auctions>{ transform() }) runOn Base(call, environment)
            }

            patch("accept-round") {
                (ReceiveContextual<AcceptRound>()) * AcceptRound * Respond<AcceptedRound>{ transform() } runOn Base(call, environment)
            }
            get("all"){
                (ReceiveContextual(GetAuctions) *
                        IsGranted("READ_AUCTION") *
                        ReadAllAuctions *
                        Respond<Auctions>{ transform() }
                        ) runOn Base(call, environment)
            }
            route("bidder") {
                post("import") {
                    (ReceiveContextual<ImportBidders>() *
                            ImportBidders *
                            Respond<Auction>{ transform() }) runOn Base(call, environment)
                }
                delete("delete"){
                    // will delete all listed bidders
                    (ReceiveContextual<DeleteBidders>() * Fail("Not Implemented") * Respond{ transform() }) runOn Base(call, environment)
                }
            }

        }
    }
