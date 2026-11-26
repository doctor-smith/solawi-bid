package org.solyton.solawi.bid.module.shares.routing

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.application.useApplicationTestDatabase
import org.solyton.solawi.bid.module.application.routing.*
import org.solyton.solawi.bid.module.banking.repository.createFiscalYear
import org.solyton.solawi.bid.module.distribution.repository.createDistributionPoint
import org.solyton.solawi.bid.module.shares.application.SHARE_MANAGEMENT_APPLICATION_NAME
import org.solyton.solawi.bid.module.shares.data.api.*
import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason as InternalReason
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy as InternalChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus as InternalStatus
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.values.ShareOfferId
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.repository.*
import org.solyton.solawi.bid.module.shares.schema.CoSubscribersTable
import org.solyton.solawi.bid.module.shares.schema.PricingType as StoredPricingType
import org.solyton.solawi.bid.module.shares.schema.ShareOfferEntity
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionStatusHistory
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionStatusHistoryEntry
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.values.ModifierId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserId
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class SharesTenantRoutingTests {
    @Api @Test
    fun shareOfferUpdateChecksTheCurrentAndDestinationShareTypes() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val sourceToken = client.getTestToken(SOURCE_USER)
            val destinationToken = client.getTestToken(DESTINATION_USER)
            val (providers, source, destination) = transaction(database) { seedShares() }

            val update = UpdateShareOffer(
                source.offerId.toString(), providers.destination.organizationId.toString(),
                destination.typeId.toString(), destination.fiscalYearId.toString(), 28.0, PricingType.FIXED, false
            )
            fun storedOffer() = transaction(database) {
                val offer = assertNotNull(ShareOfferEntity.findById(source.offerId))
                offer.toApiType() to offer.modifiedBy
            }
            val before = storedOffer()
            val path = "shares/offers/update"
            val updateInSourceContext = update.copy(providerId = providers.source.organizationId.toString())
            assertEquals(
                HttpStatusCode.Forbidden, client.patchInContext(path, sourceToken, providers.source.contextId, updateInSourceContext)
            )
            assertEquals(before, storedOffer())
            assertEquals(
                HttpStatusCode.Forbidden,
                client.patchInContext(path, destinationToken, providers.destination.contextId, update)
            )
            assertEquals(before, storedOffer())

            transaction(database) { providers.destination.grantTo(providers.sourceUserId) }
            assertEquals(HttpStatusCode.OK, client.patchInContext(path, sourceToken, providers.source.contextId, update))
            val (after, changedBy) = storedOffer()
            assertEquals(update.shareTypeId, after.shareType.id)
            assertEquals(update.fiscalYearId, after.fiscalYear.id)
            assertEquals(update.price, after.price)
            assertEquals(providers.sourceUserId, changedBy)
        }
    }

    @Api @Test
    fun mixedStatusBatchIsRejectedWithoutChangingSubscriptionsOrHistory() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) { seedShares() }
            val path = "shares/subscriptions/update-statuses"

            fun subscriptionStatuses() = transaction(database) {
                ShareSubscriptionEntity.all().associate { it.id.value to (it.status.name to it.statusUpdatedAt) }
            }
            fun historyCount() = transaction(database) { ShareSubscriptionStatusHistoryEntry.count() }
            val before = subscriptionStatuses()
            val beforeHistory = historyCount()

            val sourceProvider = ProviderId(providers.source.organizationId.toString())
            val destinationProvider = ProviderId(providers.destination.organizationId.toString())
            val statusUpdate = UpdateShareStatus(
                sourceProvider, source.subscriptionId, ShareStatus.ActivationRejected,
                ChangeReason.REQUIREMENTS_NOT_MET, ChangedBy.PROVIDER,
                ModifierId(providers.sourceUserId.toString()), "Test status transition"
            )
            val mixed = UpdateShareStatuses(
                sourceProvider,
                listOf(statusUpdate, statusUpdate.copy(shareSubscriptionId = destination.subscriptionId))
            )
            val context = providers.source.contextId
            assertEquals(HttpStatusCode.Forbidden, client.patchInContext(path, token, context, mixed))
            assertEquals(
                HttpStatusCode.Forbidden,
                client.patchInContext(path, token, context, UpdateShareStatuses(destinationProvider, emptyList()))
            )
            assertEquals(HttpStatusCode.OK, client.patchInContext(path, token, context, UpdateShareStatuses(sourceProvider, emptyList())))
            assertEquals(before, subscriptionStatuses())
            assertEquals(beforeHistory, historyCount())

            val ownBatch = UpdateShareStatuses(sourceProvider, listOf(statusUpdate))
            assertEquals(HttpStatusCode.OK, client.patchInContext(path, token, context, ownBatch))
            val after = subscriptionStatuses()
            assertEquals(ShareStatus.ActivationRejected.toString(), after.getValue(source.subscriptionUuid).first)
            assertEquals(before.getValue(destination.subscriptionUuid), after.getValue(destination.subscriptionUuid))
            assertEquals(beforeHistory + 1, historyCount())
        }
    }

    @Api @Test
    fun historyBatchRequiresReadAccessForEverySubscription() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) { seedShares() }

            suspend fun readHistory(ids: List<ShareSubscriptionId>) = client.get("shares/subscriptions/history") {
                bearerAuth(token)
                header(Header.CONTEXT, providers.source.contextId.toString())
                ids.forEach { parameter("share_subscription_id", it.value) }
            }
            assertEquals(HttpStatusCode.Forbidden, readHistory(listOf(source.subscriptionId, destination.subscriptionId)).status)
            val ownHistory = readHistory(listOf(source.subscriptionId))
            assertEquals(HttpStatusCode.OK, ownHistory.status)
            val body = Json.decodeFromString(ResultSerializer<ShareSubscriptionStatusHistories>(), ownHistory.bodyAsText())
            val history = assertIs<Result.Success<ShareSubscriptionStatusHistories>>(body).data.all.single()
            assertEquals(source.subscriptionId, history.shareSubscriptionId)
            assertEquals(ChangeReason.INITIAL_CREATION, history.entries.single().reason)
        }
    }

    @Api @Test
    fun rolloverChecksEachSourceAndTheTargetOfferBeforePersistingAnyChanges() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) { seedShares() }
            val nextOffer = transaction(database) {
                createTestShareStatus(InternalStatus.Subscribed)
                createTestShareStatus(InternalStatus.RollingOver)
                ShareSubscriptionEntity.all().forEach { it.status = statusEntity(InternalStatus.Subscribed) }
                val start = DateTime(2027, 1, 1, 0, 0, DateTimeZone.UTC)
                val year = createFiscalYear(providers.source.organizationId, start, start.plusYears(1).minusDays(1), UUID_ZERO)
                ShareOfferId(createShareOffer(source.typeId, year.id.value, 20.0, StoredPricingType.FIXED, false, UUID_ZERO).id.value.toString())
            }
            val request = RolloverShareSubscriptions(
                ProviderId(providers.source.organizationId.toString()), listOf(source.subscriptionId), nextOffer,
                ChangedBy.PROVIDER, UserId(providers.sourceUserId.toString())
            )
            suspend fun rollover(data: RolloverShareSubscriptions) = client.post("shares/subscriptions/rollover") {
                bearerAuth(token)
                header(Header.CONTEXT, providers.source.contextId.toString())
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(data))
            }.status
            fun persistedState() = transaction(database) {
                ShareSubscriptionEntity.all().associate { it.id.value to it.status.name } to ShareSubscriptionStatusHistoryEntry.count()
            }
            val before = persistedState()

            assertEquals(HttpStatusCode.Forbidden, rollover(request.copy(shareSubscriptionIds = listOf(source.subscriptionId, destination.subscriptionId))))
            assertEquals(before, persistedState())
            assertEquals(HttpStatusCode.Forbidden, rollover(request.copy(toShareOfferId = ShareOfferId(destination.offerId.toString()))))
            assertEquals(before, persistedState())

            assertEquals(HttpStatusCode.OK, rollover(request))
            val (statuses, historyCount) = persistedState()
            assertEquals(ShareStatus.RollingOver.toString(), statuses.getValue(source.subscriptionUuid))
            assertEquals(before.first.getValue(destination.subscriptionUuid), statuses.getValue(destination.subscriptionUuid))
            assertEquals(ShareStatus.PendingActivation.toString(), (statuses - before.first.keys).values.single())
            assertEquals(before.second + 2, historyCount)
        }
    }

    private fun Transaction.seedShares(): SharesFixture {
        SchemaUtils.createMissingTablesAndColumns(ShareSubscriptionStatusHistory, CoSubscribersTable)
        createTestShareStatus(InternalStatus.PendingActivation)
        createTestShareStatus(InternalStatus.ActivationRejected)
        val providers = createProviders(SHARE_MANAGEMENT_APPLICATION_NAME, "MANAGE_SHARES", "MANAGE_SHARES")
        val subscriber = UserProfileEntity.new {
            user = assertNotNull(UserEntity.findById(providers.sourceUserId))
            firstName = "Test"
            lastName = "Subscriber"
            createdBy = UUID_ZERO
        }
        return SharesFixture(
            providers,
            createProviderShares(providers.source, subscriber.id.value),
            createProviderShares(providers.destination, subscriber.id.value)
        )
    }

    private fun Transaction.createProviderShares(provider: Provider, subscriberId: UUID): ProviderShares {
        val start = DateTime(2026, 1, 1, 0, 0, DateTimeZone.UTC)
        val fiscalYear = createFiscalYear(provider.organizationId, start, start.plusYears(1).minusDays(1), UUID_ZERO)
        val type = createShareType(provider.organizationId, "Test shares", "fixture", "Test description", UUID_ZERO)
        val offer = createShareOffer(type.id.value, fiscalYear.id.value, 20.0, StoredPricingType.FIXED, false, UUID_ZERO)
        val point = createDistributionPoint("Test point", null, provider.organizationId, UUID_ZERO)
        val subscription = createShareSubscription(
            offer.id.value, subscriberId, point.id.value, fiscalYear.id.value, 1, null, null, UUID_ZERO
        )
        ShareSubscriptionStatusHistoryEntry.new {
            shareSubscription = subscription
            toStatus = subscription.status
            reason = InternalReason.INITIAL_CREATION
            changedBy = InternalChangedBy.SYSTEM
        }
        return ProviderShares(type.id.value, offer.id.value, fiscalYear.id.value, subscription.id.value)
    }

    private data class SharesFixture(val providers: Providers, val source: ProviderShares, val destination: ProviderShares)

    private data class ProviderShares(val typeId: UUID, val offerId: UUID, val fiscalYearId: UUID, val subscriptionUuid: UUID) {
        val subscriptionId get() = ShareSubscriptionId(subscriptionUuid.toString())
    }
}
