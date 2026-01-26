package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.bid.action.db.AddBidders
import org.solyton.solawi.bid.module.bid.action.db.SearchBidderMails
import org.solyton.solawi.bid.module.bid.data.api.AddBidders
import org.solyton.solawi.bid.module.bid.data.api.BidderMails
import org.solyton.solawi.bid.module.bid.data.api.SearchBidderData

@KtorDsl
fun <BidEnv> Routing.bidders(
    environment: BidEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BidEnv : KTorEnv, BidEnv: DbEnv =
    authenticate{
        val transform = environment.transformException
        route("bidders") {
            patch("search") {
                ReceiveContextual<SearchBidderData>() * SearchBidderMails * Respond<BidderMails>{ transform() } runOn Base(call, environment)
            }
            post("add") {
                ReceiveContextual<AddBidders>() * AddBidders * Respond<Unit>{ transform() } runOn Base(call, environment)
            }

        }
    }
