package org.solyton.solawi.bid.module.shares.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.*
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.application.repository.contextIdOf
import org.solyton.solawi.bid.module.permission.action.db.IsGranted
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedOneOf
import org.solyton.solawi.bid.module.permission.action.db.IsGrantedOneOfInAllDerivedContexts
import org.solyton.solawi.bid.module.permission.action.db.no
import org.solyton.solawi.bid.module.permission.action.db.rights
import org.solyton.solawi.bid.module.shares.action.api.*
import org.solyton.solawi.bid.module.shares.application.SHARE_MANAGEMENT_APPLICATION_NAME
import org.solyton.solawi.bid.module.shares.data.api.*
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.exception.ShareException
import org.solyton.solawi.bid.module.shares.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.shares.schema.ShareTypeEntity
import org.solyton.solawil.bid.module.shares.data.toUUID
import org.solyton.solawil.bid.module.user.data.toUUID
import java.util.*

// const val SHARE_APPLICATION = "AUCTIONS"

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
                IsGrantedOneOf(
                    rights(
                        "CREATE_SHARE_TYPES",
                        "MANAGE_SHARE_TYPES",
                        "MANAGE_SHARES"
                    )
                ) { contextual ->
                    val providerId = UUID.fromString(contextual.data.providerId)
                    contextIdOf(providerId, SHARE_MANAGEMENT_APPLICATION_NAME)
                } *
                CreateShareType() *
                Respond<ShareType> { transform() } runOn Base(call, environment)
            }
            patch("update"){
                ReceiveContextual<UpdateShareType>() *
                IsGrantedOneOfInAllDerivedContexts(
                    rights = rights(
                        "UPDATE_SHARE_TYPES",
                        "MANAGE_SHARE_TYPES",
                        "MANAGE_SHARES"
                    )
                ) { contextual ->
                    val data = contextual.data
                    val actualProviderId = ShareTypeEntity.findById(UUID.fromString(data.id))
                        ?.providerId
                        ?: throw ShareException.NoSuchShareType(data.id)
                    setOf(actualProviderId, UUID.fromString(data.providerId))
                        .map { contextIdOf(it, SHARE_MANAGEMENT_APPLICATION_NAME) }
                        .toSet()
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
                    )
                ) { contextual ->
                    val providerId = contextual.data.providerId
                    contextIdOf(providerId, SHARE_MANAGEMENT_APPLICATION_NAME)
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
                    contextIdOf(providerId, SHARE_MANAGEMENT_APPLICATION_NAME)
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
                    contextIdOf(providerId, SHARE_MANAGEMENT_APPLICATION_NAME)
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
                    contextIdOf(providerId, SHARE_MANAGEMENT_APPLICATION_NAME)
                } *
                ReadShareOffersByProvider() *
                Respond<ShareOffers> { transform() } runOn Base(call, environment)
            }
            get("personal") {
                ReceiveContextual {
                    Unit
                } *
                ReadPersonalShareOffers() *
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
                    contextIdOf(providerId, SHARE_MANAGEMENT_APPLICATION_NAME)
                } *
                CreateShareSubscription() *
                Respond<ShareSubscription> { transform() } runOn Base(call, environment)
            }
            post("rollover"){
                ReceiveContextual<RolloverShareSubscriptions>() *
                IsGrantedOneOfInAllDerivedContexts(
                    rights = rights(
                        "CREATE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    )
                ) { contextual ->
                    val data = contextual.data
                    val sourceProviderIds = data.shareSubscriptionIds.map { id ->
                        ShareSubscriptionEntity.findById(id.toUUID())
                            ?.shareOffer
                            ?.shareType
                            ?.providerId
                            ?: throw ShareException.NoSuchShareSubscription(id.value)
                    }
                    val targetProviderId = ShareOfferEntity.findById(data.toShareOfferId.toUUID())
                        ?.shareType
                        ?.providerId
                        ?: throw ShareException.NoSuchShareOffer(data.toShareOfferId.value)
                    (sourceProviderIds + targetProviderId + data.providerId.toUUID())
                        .map { contextIdOf(it, SHARE_MANAGEMENT_APPLICATION_NAME) }
                        .toSet()
                } *
                RolloverShareSubscription() *
                Respond<ShareSubscriptions> { transform() } runOn Base(call, environment)
            }
            patch("transfer-ownership") {
                ReceiveContextual<TransferOwnership>() *
                IsGrantedOneOfInAllDerivedContexts(
                    rights = rights(
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    )
                ) { contextual ->
                    val data = contextual.data
                    val actualProviderId = ShareSubscriptionEntity.findById(data.shareSubscriptionId.toUUID())
                        ?.shareOffer
                        ?.shareType
                        ?.providerId
                        ?: throw ShareException.NoSuchShareSubscription(data.shareSubscriptionId.value)
                    setOf(actualProviderId, data.providerId.toUUID())
                        .map { contextIdOf(it, SHARE_MANAGEMENT_APPLICATION_NAME) }
                        .toSet()
                } *
                TransferOwnership() *
                Respond { transform() } runOn Base(call, environment)
            }
            patch("update"){
                ReceiveContextual<UpdateShareSubscription>() *
                IsGrantedOneOfInAllDerivedContexts(
                    rights = rights(
                        "UPDATE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    )
                ) { contextual ->
                    val data = contextual.data
                    val sourceProviderId = ShareSubscriptionEntity.findById(UUID.fromString(data.id))
                        ?.shareOffer
                        ?.shareType
                        ?.providerId
                        ?: throw ShareException.NoSuchShareSubscription(data.id)
                    val targetProviderId = ShareOfferEntity.findById(UUID.fromString(data.shareOfferId))
                        ?.shareType
                        ?.providerId
                        ?: throw ShareException.NoSuchShareOffer(data.shareOfferId)
                    setOf(sourceProviderId, targetProviderId, UUID.fromString(data.providerId))
                        .map { contextIdOf(it, SHARE_MANAGEMENT_APPLICATION_NAME) }
                        .toSet()
                } *
                UpdateShareSubscription() *
                Respond<ShareSubscription> { transform() } runOn Base(call, environment)
            }
            patch("update-status") {
                ReceiveContextual<UpdateShareStatus>() *
                IsGrantedOneOfInAllDerivedContexts(
                    rights = rights(
                        "UPDATE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    )
                ) { contextual ->
                    val status = contextual.data
                    val actualProviderId = ShareSubscriptionEntity
                        .findById(status.shareSubscriptionId.toUUID())
                        ?.shareOffer
                        ?.shareType
                        ?.providerId
                        ?: throw ShareException.NoSuchShareSubscription(status.shareSubscriptionId.value)
                    setOf(status.providerId.toUUID(), actualProviderId)
                        .map { contextIdOf(it, SHARE_MANAGEMENT_APPLICATION_NAME) }
                        .toSet()
                } *
                UpdateShareStatus() *
                Respond<ShareSubscriptions> { transform() } runOn Base(call, environment)
            }
            patch("update-statuses") {
                ReceiveContextual<UpdateShareStatuses>() *
                IsGrantedOneOfInAllDerivedContexts(
                    rights = rights(
                        "UPDATE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARE_SUBSCRIPTIONS",
                        "MANAGE_SHARES"
                    )
                ) { contextual ->
                    val payloadProviderId = contextual.data.providerId.toUUID()
                    val claimedProviderIds = contextual.data.all.map { it.providerId.toUUID() }
                    val actualProviderIds = contextual.data.all.map { status ->
                        ShareSubscriptionEntity.findById(status.shareSubscriptionId.toUUID())
                            ?.shareOffer
                            ?.shareType
                            ?.providerId
                            ?: throw ShareException.NoSuchShareSubscription(status.shareSubscriptionId.value)
                    }
                    (listOf(payloadProviderId) + claimedProviderIds + actualProviderIds)
                        .map { contextIdOf(it, SHARE_MANAGEMENT_APPLICATION_NAME) }
                        .toSet()
                } *
                UpdateShareStatuses() *
                Respond<ShareSubscriptions> { transform() } runOn Base(call, environment)
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
                    contextIdOf(providerId, SHARE_MANAGEMENT_APPLICATION_NAME)
                } *
                ReadShareShareSubscriptionsByProvider() * Respond<ShareSubscriptions> { transform() } runOn Base(call, environment)
            }
            get("history") {
                ReceiveContextual { params ->
                    val ids = (params.getAll("share_subscription_id").orEmpty()).map{ ShareSubscriptionId(it) }
                    ReadShareSubscriptionStatusHistories(ids)
                } *
                ReadShareSubscriptionStatusHistories() *
                Respond< ShareSubscriptionStatusHistories> { transform() } runOn Base(call, environment)
            }
            get("personal") {
                ReceiveContextual{ params ->
                    ReadPersonalShareSubscriptions(listOf())
                } *
                IsGranted("", no) *
                ReadPersonalShareSubscriptions() *
                Respond<ShareSubscriptions> { transform() } runOn Base(call,environment)

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
                    contextIdOf(providerId, SHARE_MANAGEMENT_APPLICATION_NAME)

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
