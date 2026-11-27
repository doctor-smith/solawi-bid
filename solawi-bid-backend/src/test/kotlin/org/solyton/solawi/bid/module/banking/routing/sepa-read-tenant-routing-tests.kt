package org.solyton.solawi.bid.module.banking.routing

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.application.useApplicationTestDatabase
import org.solyton.solawi.bid.module.application.routing.*
import org.solyton.solawi.bid.module.banking.data.api.SepaCollections
import org.solyton.solawi.bid.module.banking.data.api.SepaMandates
import org.solyton.solawi.bid.module.banking.data.api.SepaPaymentLinks
import org.solyton.solawi.bid.module.banking.repository.createPayment
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentEntity
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentLinkEntity
import org.solyton.solawi.bid.module.banking.schema.SuccessorKind
import org.solyton.solawi.bid.module.testFramework.getTestToken
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class SepaReadTenantRoutingTests {
    @Api @Test
    fun creditorMandatesRequireReadPermissionInTheRequestedLegalEntityContext() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) {
                seedBanking().also { grantProviderRight(it.providers.source, "READ_SEPA_MANDATES") }
            }
            val path = "mandates/by-creditors-legal-entity"
            val context = providers.source.contextId
            val forbidden = client.fetchSepa(path, token, context, "legal_entity" to destination.legalEntityId.value)
            assertEquals(HttpStatusCode.Forbidden, forbidden.status)
            val own = client.fetchSepa(path, token, context, "legal_entity" to source.legalEntityId.value)
            assertEquals(listOf(source.mandateId), own.success<SepaMandates>().all.map { it.sepaMandateId })
        }
    }

    @Api @Test
    fun collectionsRequireReadPermissionInTheRequestedLegalEntityContext() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) {
                seedBanking().also { grantProviderRight(it.providers.source, "READ_SEPA_COLLECTIONS") }
            }
            val path = "collections/by-legal-entity"
            val context = providers.source.contextId
            val forbidden = client.fetchSepa(path, token, context, "legal_entity" to destination.legalEntityId.value)
            assertEquals(HttpStatusCode.Forbidden, forbidden.status)
            val own = client.fetchSepa(path, token, context, "legal_entity" to source.legalEntityId.value)
            assertEquals(listOf(source.collectionId), own.success<SepaCollections>().all.map { it.sepaCollectionId })
        }
    }

    @Api @Test
    fun paymentLinksRequireReadPermissionInTheRequestedLegalEntityContext() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) {
                seedBanking().also {
                    seedPaymentLink(it.source)
                    seedPaymentLink(it.destination)
                }
            }
            val path = "payment-links/by-legal-entity"
            val context = providers.source.contextId
            val forbidden = client.fetchSepa(path, token, context, "legal_entity" to destination.legalEntityId.value)
            assertEquals(HttpStatusCode.Forbidden, forbidden.status)
            val own = client.fetchSepa(path, token, context, "legal_entity" to source.legalEntityId.value)
            assertEquals(listOf(source.paymentId), own.success<SepaPaymentLinks>().all.map { it.predecessorId })
        }
    }

    @Api @Test
    fun messageDownloadRequiresPermissionForTheStoredMessage() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, _, destination) = transaction(database) { seedBanking() }

            val forbidden = client.fetchSepa(
                "messages/download", token, providers.source.contextId, "message_id" to destination.messageId.value
            )
            assertEquals(HttpStatusCode.Forbidden, forbidden.status)
        }
    }

    @Api @Test
    fun personalMandatesUseTheAuthenticatedDebtor() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val sourceToken = client.getTestToken(SOURCE_USER)
            val destinationToken = client.getTestToken(DESTINATION_USER)
            val (providers, source, destination) = transaction(database) { seedBanking() }
            val path = "mandates/personal/all"

            assertEquals(HttpStatusCode.Unauthorized, client.fetchSepa(path, null, providers.source.contextId).status)
            val sourceMandates = client.fetchSepa(path, sourceToken, providers.destination.contextId).success<SepaMandates>()
            val destinationMandates = client.fetchSepa(path, destinationToken, providers.source.contextId).success<SepaMandates>()
            assertEquals(listOf(source.mandateId), sourceMandates.all.map { it.sepaMandateId })
            assertEquals(listOf(destination.mandateId), destinationMandates.all.map { it.sepaMandateId })
        }
    }

    @Api @Test
    fun personalPaymentLinksUseTheAuthenticatedDebtor() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val sourceToken = client.getTestToken(SOURCE_USER)
            val destinationToken = client.getTestToken(DESTINATION_USER)
            val (providers, source, destination) = transaction(database) {
                seedBanking().also {
                    seedPaymentLink(it.source)
                    seedPaymentLink(it.destination)
                }
            }
            val path = "payment-links/personal"

            assertEquals(HttpStatusCode.Unauthorized, client.fetchSepa(path, null, providers.source.contextId).status)
            val sourceLinks = client.fetchSepa(path, sourceToken, providers.destination.contextId).success<SepaPaymentLinks>()
            val destinationLinks = client.fetchSepa(path, destinationToken, providers.source.contextId).success<SepaPaymentLinks>()
            assertEquals(listOf(source.paymentId), sourceLinks.all.map { it.predecessorId })
            assertEquals(listOf(destination.paymentId), destinationLinks.all.map { it.predecessorId })
        }
    }

    private fun Transaction.seedPaymentLink(bank: ProviderBanking) {
        val original = assertNotNull(SepaPaymentEntity.findById(UUID.fromString(bank.paymentId.value)))
        val nextPayment = createPayment(
            UUID_ZERO, original.mandate.id.value, original.collection.id.value, 12.0,
            LocalDate(2026, 12, 2), ignoreOldPayments = true
        )
        SepaPaymentLinkEntity.new {
            predecessor = original
            successor = nextPayment
            kind = SuccessorKind.AD_HOC
            createdBy = UUID_ZERO
        }
    }

    private suspend fun HttpClient.fetchSepa(
        path: String,
        token: String?,
        context: UUID,
        query: Pair<String, String>? = null
    ) = get("banking/sepa/$path") {
        token?.let { bearerAuth(it) }
        header(Header.CONTEXT, context.toString())
        query?.let { (name, value) -> parameter(name, value) }
    }

    private suspend inline fun <reified T : Any> HttpResponse.success(): T {
        assertEquals(HttpStatusCode.OK, status)
        return assertIs<Result.Success<T>>(Json.decodeFromString(ResultSerializer<T>(), bodyAsText())).data
    }
}
