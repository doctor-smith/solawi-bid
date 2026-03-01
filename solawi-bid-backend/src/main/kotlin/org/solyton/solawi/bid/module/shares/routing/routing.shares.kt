package org.solyton.solawi.bid.module.shares.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.NotImplemented
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.application.repository.contextIdOf
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedOneOf
import org.solyton.solawi.bid.module.permission.action.db.no
import org.solyton.solawi.bid.module.permission.action.db.rights
import org.solyton.solawi.bid.module.shares.action.api.*
import org.solyton.solawi.bid.module.shares.data.api.*
import org.solyton.solawil.bid.module.user.data.toUUID
import java.util.*

const val SHARE_APPLICATION = "AUCTIONS"

@KtorDsl
fun <SharesEnv> Routing.shares(
    environment: SharesEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where SharesEnv : KTorEnv, SharesEnv: DbEnv =
authenticate {
    val transform = environment.transformException
    route("shares") {
        route("types") {
            post("create") {
                ReceiveContextual<CreateShareType>() *
                IsGrantedOneOf(rights(
                    "CREATE_SHARE_TYPES",
                    "MANAGE_SHARE_TYPES",
                    "MANAGE_SHARES"
                ),
                    no, // todo:permission enable access check
                ) { contextual ->
                    val providerId = UUID.fromString(contextual.data.providerId)
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                CreateShareType() *
                Respond<ShareType> { transform() } runOn Base(call, environment)
            }
            patch("update"){
                ReceiveContextual<UpdateShareType>() *
                IsGrantedOneOf(
                    rights(
                        "UPDATE_SHARE_TYPES",
                        "MANAGE_SHARE_TYPES",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = UUID.fromString(contextual.data.providerId)
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                UpdateShareType() *
                Respond<ShareType> { transform() } runOn Base(call, environment)
            }
            get("all") {
                @Suppress("UnsafeCallOnNullableType")
                ReceiveContextual{
                    params -> ReadShareTypesByProvider(
                    UUID.fromString(params["provider"]!!)
                    )
                } *
                IsGrantedOneOf(
                    rights(
                        "READ_SHARE_TYPES",
                        "MANAGE_SHARE_TYPES",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = contextual.data.providerId
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                ReadShareTypesByProvider() *
                Respond<ShareTypes> { transform() } runOn Base(call, environment)
            }
            delete {
                NotImplemented() * Respond<Unit> { transform() } runOn Base(call, environment)
            }
        }
        route("offers") {
            post("create") {
                ReceiveContextual<CreateShareOffer>() *
                IsGrantedOneOf(
                    rights(
                        "CREATE_SHARE_OFFERS",
                        "MANAGE_SHARE_OFFERS",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = UUID.fromString(contextual.data.providerId)
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                CreateShareOffer() *
                Respond<ShareOffer> { transform() } runOn Base(call, environment)
            }
            patch("update"){
                ReceiveContextual<UpdateShareOffer>() *
                IsGrantedOneOf(
                    rights(
                        "UPDATE_SHARE_OFFERS",
                        "MANAGE_SHARE_OFFERS",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = UUID.fromString(contextual.data.providerId)
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                UpdateShareOffer() *
                Respond<ShareOffer> { transform() } runOn Base(call, environment)
            }
            get("all") {
                @Suppress("UnsafeCallOnNullableType")
                ReceiveContextual{
                    params -> ReadShareOffersByProvider(
                        UUID.fromString(params["provider"]!!),
                        params.getAll("fiscalYearIds")?.map{
                            UUID.fromString(it)
                        }?.toSet().orEmpty()
                    )
                } *
                IsGrantedOneOf(
                    rights(
                        "READ_SHARE_OFFERS",
                        "MANAGE_SHARE_OFFERS",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = contextual.data.providerId
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                ReadShareOffersByProvider() *
                Respond<ShareOffers> { transform() } runOn Base(call, environment)
            }
            delete {
                NotImplemented() * Respond<Unit> { transform() } runOn Base(call, environment)
            }
        }
        route("subscriptions") {
            post("create") {
                ReceiveContextual<CreateShareSubscription>() *
                IsGrantedOneOf(
                    rights(
                        "CREATE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = UUID.fromString(contextual.data.providerId)
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                CreateShareSubscription() *
                Respond<ShareSubscription> { transform() } runOn Base(call, environment)
            }
            patch("update"){
                ReceiveContextual<UpdateShareSubscription>() *
                IsGrantedOneOf(
                    rights(
                        "UPDATE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = UUID.fromString(contextual.data.providerId)
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                UpdateShareSubscription() *
                Respond<ShareSubscription> { transform() } runOn Base(call, environment)
            }
            patch("update-status") {
                ReceiveContextual<UpdateShareStatus>() *
                IsGrantedOneOf(
                    rights(
                        "UPDATE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = contextual.data.providerId.toUUID()
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                UpdateShareStatus() *
                Respond<ShareSubscription> { transform() } runOn Base(call, environment)
            }
            get("all") {
                @Suppress("UnsafeCallOnNullableType")
                ReceiveContextual{
                    params -> ReadShareSubscriptionsByProvider(
                        UUID.fromString(params["provider"]!!),
                        params.getAll("fiscalYearIds")?.map{
                            UUID.fromString(it)
                        }?.toSet().orEmpty()
                    )
                } *
                IsGrantedOneOf(
                    rights(
                        "READ_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    ),
                    no // todo:permission enable access check
                ) { contextual ->
                    val providerId = contextual.data.providerId
                    contextIdOf(providerId, SHARE_APPLICATION)
                } *
                ReadShareShareSubscriptionsByProvider() * Respond<ShareSubscriptions> { transform() } runOn Base(call, environment)
            }
            delete {
                NotImplemented() * Respond<Unit> { transform() } runOn Base(call, environment)
            }
            post("import") {
                ReceiveContextual<ImportShareSubscriptions>() *
                IsGrantedOneOf(
                    rights("IMPORT_SHARE_SUBSCRIPTIONS"),
                    no
                ) { contextual ->
                    val providerId = contextual.data.providerId.toUuid()
                    contextIdOf(providerId, SHARE_APPLICATION)

                } *
                ImportShareSubscriptions() *
                Respond<ShareSubscriptions> { transform() } runOn Base(call, environment)
            }
        }

        get() {
            NotImplemented() * Respond<Unit> { transform() } runOn Base(call, environment)
        }
    }
}
