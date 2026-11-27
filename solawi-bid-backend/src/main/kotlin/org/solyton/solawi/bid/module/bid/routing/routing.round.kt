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
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedInDerivedContext
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedOneOf
import org.solyton.solawi.bid.module.permission.action.db.rights
import java.util.UUID

private val roundManagementRights = rights("UPDATE_AUCTION", "MODERATE_AUCTION")


@KtorDsl
fun <BidEnv> Routing.round(
    environment: BidEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where BidEnv : KTorEnv, BidEnv: DbEnv =
    authenticate{
        val transform = environment.transformException
        route("round") {
            post("create") {
                ReceiveContextual<CreateRound>() *
                    IsGrantedInDerivedContext<CreateRound>("UPDATE_AUCTION") { request ->
                        readAuction(UUID.fromString(request.data.auctionId)).context.id.value
                    } *
                    CreateRound * Respond { transform() } runOn Base(call, environment)
            }
            patch("change-state") {
                ReceiveContextual<ChangeRoundState>() *
                    IsGrantedOneOf<ChangeRoundState>(roundManagementRights) { request ->
                        readRound(request.data.roundId).auction.context.id.value
                    } *
                    ChangeRoundState * Respond { transform() } runOn Base(call, environment)
            }

            patch("export-results") {
                ReceiveContextual<ExportBidRound>() *
                    IsGrantedInDerivedContext<ExportBidRound>("Export Results") { request ->
                        readRound(request.data.roundId, request.data.auctionId).auction.context.id.value
                    } *
                    ExportResults * Respond { transform() } runOn Base(call, environment)
            }

            patch("evaluate") {
                ReceiveContextual<EvaluateBidRound>() *
                    IsGrantedOneOf<EvaluateBidRound>(roundManagementRights) { request ->
                        readRound(request.data.roundId, request.data.auctionId).auction.context.id.value
                    } *
                    EvaluateBidRound * Respond { transform() } runOn Base(call, environment)
            }

            patch("pre-evaluate") {
                ReceiveContextual<PreEvaluateBidRound>() *
                    IsGrantedOneOf<PreEvaluateBidRound>(roundManagementRights) { request ->
                        readRound(request.data.roundId, request.data.auctionId).auction.context.id.value
                    } *
                    PreEvaluateBidRound * Respond { transform() } runOn Base(call, environment)
            }

            post("add-comment") {
                ReceiveContextual<CommentOnRound>() *
                    IsGrantedOneOf<CommentOnRound>(roundManagementRights) { request ->
                        readRound(request.data.roundId).auction.context.id.value
                    } *
                    CommentOnRound * Respond { transform() } runOn Base(call, environment)
            }
        }
    }
