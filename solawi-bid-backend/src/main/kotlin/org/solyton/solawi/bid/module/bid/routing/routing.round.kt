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
import org.solyton.solawi.bid.module.bid.action.db.*
import org.solyton.solawi.bid.module.bid.data.api.*


@KtorDsl
fun <BidEnv> Routing.round(
    environment: BidEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BidEnv : KTorEnv, BidEnv: DbEnv =
    authenticate{
        val transform = environment.transformException
        route("round") {
            post("create") {
                ReceiveContextual<CreateRound>() * CreateRound * Respond{ transform() }runOn Base(call,environment)
            }
            patch("change-state") {
                ReceiveContextual<ChangeRoundState>() * ChangeRoundState *  Respond{ transform() } runOn Base(call,environment)
            }

            patch("export-results") {
                ReceiveContextual<ExportBidRound>() * ExportResults * Respond{ transform() } runOn Base(call, environment)
            }

            patch("evaluate") {
                ReceiveContextual<EvaluateBidRound>() * EvaluateBidRound * Respond{ transform() } runOn Base(call, environment)
            }

            patch("pre-evaluate") {
                ReceiveContextual<PreEvaluateBidRound>() * PreEvaluateBidRound * Respond{ transform() } runOn Base(call, environment)
            }

            post("add-comment") {
                ReceiveContextual<CommentOnRound>() * CommentOnRound * Respond { transform() } runOn Base(call, environment)
            }
        }
    }
