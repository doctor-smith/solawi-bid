package org.solyton.solawi.bid.module.banking.routing

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.application.useApplicationTestDatabase
import org.solyton.solawi.bid.module.application.routing.*
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.testFramework.getTestToken
import java.util.UUID
import org.solyton.solawi.bid.module.banking.data.api.PaymentExecutionStatus as ApiPaymentStatus
import org.solyton.solawi.bid.module.banking.data.api.SepaSequenceType as ApiSequenceType
import kotlin.test.assertEquals

class SepaTenantRoutingTests {
    @Api @Test
    fun collectionCreationRequiresAccessToCreditorAndBankAccountOwner() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) { seedBanking() }
            val create = CreateSepaCollection(
                creditorIdentifierId = source.creditorId,
                creditorBankAccountId = destination.accountId,
                mandateReferencePrefix = MandateReferencePrefix("TEST"),
                collectionKey = SepaCollectionKey("new-collection"),
                remittanceInformation = RemittanceInformation("Test"),
                sepaSequenceType = ApiSequenceType.FRST,
                localInstrument = null
            )
            fun storedCollections() = transaction(database) { SepaCollectionEntity.all().map { it.toApiType() }.toSet() }
            val before = storedCollections()
            val path = "collections/create"
            assertEquals(HttpStatusCode.Forbidden, client.postSepa(path, token, providers.source, create).status)
            assertEquals(before, storedCollections())

            val ownCollection = create.copy(creditorBankAccountId = source.accountId)
            assertEquals(HttpStatusCode.OK, client.postSepa(path, token, providers.source, ownCollection).status)
            val persisted = (storedCollections() - before).single()
            assertEquals(source.creditorId, persisted.creditorIdentifierId)
            assertEquals(source.accountId, persisted.creditorBankAccountId)
            assertEquals(create.collectionKey, persisted.collectionKey)
        }
    }

    @Api @Test
    fun attachingAMandateChecksTheCollectionAndPreservesExistingMandateDataOnDenial() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) { seedBanking() }
            val attach = AttachSepaMandateReferenceData(
                source.mandateId, SepaMandateReferenceId(UUID.randomUUID().toString()), 12.0, destination.collectionId
            )
            fun storedLinks() = transaction(database) {
                Triple(SepaMandateDataMappingEntity.count(), SepaMandateCollectionEntity.count(), SepaPaymentTemplateEntity.count())
            }
            val before = storedLinks()
            val path = "banking/sepa/mandates/attach-reference-data"
            val context = providers.source.contextId
            assertEquals(HttpStatusCode.Forbidden, client.patchInContext(path, token, context, attach))
            assertEquals(before, storedLinks())

            assertEquals(
                HttpStatusCode.OK, client.patchInContext(path, token, context, attach.copy(collectionId = source.collectionId))
            )
            assertEquals(Triple(before.first + 1, before.second, before.third), storedLinks())
            transaction(database) {
                val mapping = SepaMandateDataMappingEntity.all().single()
                assertEquals(attach.referenceId.value, mapping.referenceId.toString())
                assertEquals(source.mandateId.value, mapping.mandate.id.value.toString())
                assertEquals(source.collectionId.value, mapping.collection?.id?.value.toString())
            }
        }
    }

    @Api @Test
    fun mixedPaymentStatusBatchLeavesPaymentsAndHistoryUnchanged() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, destination) = transaction(database) { seedBanking() }
            fun storedPayments() = transaction(database) {
                SepaPaymentEntity.all().associate {
                    val payment = it.toApiType()
                    payment.sepaPaymentId to (payment to it.modifiedBy)
                }
            }
            fun historyCount() = transaction(database) { SepaPaymentStatusHistoryEntity.count() }
            val before = storedPayments()
            val beforeHistory = historyCount()
            val path = "banking/sepa/payments/update-execution-statuses"
            val update = UpdateSepaPaymentExecutionStatuses(
                ApiPaymentStatus.PAYED_MANUALLY, listOf(source.paymentId, destination.paymentId)
            )
            assertEquals(HttpStatusCode.Forbidden, client.patchInContext(path, token, providers.source.contextId, update))
            assertEquals(before, storedPayments())
            assertEquals(beforeHistory, historyCount())

            val ownBatch = update.copy(paymentIds = listOf(source.paymentId))
            assertEquals(HttpStatusCode.OK, client.patchInContext(path, token, providers.source.contextId, ownBatch))
            val after = storedPayments()
            assertEquals(before.getValue(source.paymentId).first.copy(status = update.newStatus) to providers.sourceUserId, after.getValue(source.paymentId))
            assertEquals(before.getValue(destination.paymentId), after.getValue(destination.paymentId))
            assertEquals(beforeHistory + 1, historyCount())
        }
    }

    @Api @Test
    fun messageGenerationRejectsAnEmptySelectionAfterCheckingTheCollection() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val (providers, source, _) = transaction(database) { seedBanking() }
            val generate = GenerateSepaMessageForCollection(source.collectionId, executionDate, listOf(source.paymentId))
            fun storedState() = transaction(database) {
                Triple(
                    SepaMessageEntity.count(),
                    SepaPaymentEntity.all().map { it.toApiType() }.toSet(),
                    SepaPaymentStatusHistoryEntity.count()
                )
            }
            suspend fun assertBadRequest(request: GenerateSepaMessageForCollection, message: String) {
                val before = storedState()
                val response = client.postSepa("collections/generate-sepa-message", token, providers.source, request)
                assertEquals(HttpStatusCode.BadRequest, response.status)
                val failure = Json.decodeFromString(Result.Failure.Message.serializer(), response.bodyAsText())
                assertEquals(Result.Failure.Message(message), failure)
                assertEquals(before, storedState())
            }

            val wrongDate = LocalDate(2026, 12, 2)
            assertBadRequest(generate.copy(executionDate = wrongDate), "No created payments found for execution date")
            val manuallyPaid = UpdateSepaPaymentExecutionStatuses(ApiPaymentStatus.PAYED_MANUALLY, listOf(source.paymentId))
            assertEquals(
                HttpStatusCode.OK,
                client.patchInContext("banking/sepa/payments/update-execution-statuses", token, providers.source.contextId, manuallyPaid)
            )
            assertBadRequest(generate.copy(sepaPaymentIds = null), "No created payments found")
        }
    }

    private suspend inline fun <reified T> HttpClient.postSepa(path: String, token: String, provider: Provider, data: T) =
        post("banking/sepa/$path") {
            bearerAuth(token)
            header(Header.CONTEXT, provider.contextId.toString())
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(data))
        }
}
