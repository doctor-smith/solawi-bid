package org.solyton.solawi.bid.module.bid.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.Receive
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.bid.action.db.StoreBid
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.api.BidRound


@KtorDsl
fun <BidEnv> Routing.sendBid(
    environment: BidEnv
) where BidEnv : KTorEnv, BidEnv: DbEnv =  route("bid") {
    val transform = environment.transformException
    post("send") {
        (Receive<Bid>() * StoreBid * Respond<BidRound>{ transform() }) runOn Base(call, environment)
    }
}
