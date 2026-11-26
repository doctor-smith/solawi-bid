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
import org.solyton.solawi.bid.module.banking.application.BANKING_APPLICATION_NAME
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.*
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.banking.schema.AccountType
import org.solyton.solawi.bid.module.banking.schema.LegalEntityType
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentLinks
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.repository.createAddress
import java.util.UUID
import org.joda.time.LocalDate as JodaLocalDate
import org.solyton.solawi.bid.module.banking.data.api.PaymentExecutionStatus as ApiPaymentStatus
import org.solyton.solawi.bid.module.banking.data.api.SepaSequenceType as ApiSequenceType
import org.solyton.solawi.bid.module.banking.schema.SepaSequenceType as StoredSequenceType
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

    private fun Transaction.seedBanking(): SepaFixture {
        SchemaUtils.createMissingTablesAndColumns(
            SepaMandateCollectionsTable, SepaMandateDataMappingsTable, SepaCollectionMappings,
            SepaPaymentStatusHistory, SepaPaymentLinks
        )
        val providers = createProviders(BANKING_APPLICATION_NAME, "MANAGE_BANK_TRANSACTIONS", "MANAGE_BANK_TRANSACTIONS")
        return SepaFixture(
            providers,
            createProviderBanking(providers.source, "source"),
            createProviderBanking(providers.destination, "destination")
        )
    }

    private fun Transaction.createProviderBanking(provider: Provider, name: String): ProviderBanking {
        val date = DateTime(2026, 12, 1, 0, 0, DateTimeZone.UTC)
        val address = createAddress(name, name, "Test street", "", "Test city", "", "00000", "DE", UUID_ZERO)
        val entity = createLegalEntity(
            provider.organizationId, name, "Test organization", LegalEntityType.ORGANIZATION, address.id.value, UUID_ZERO
        )
        val creditor = CreditorIdentifierEntity.new {
            legalEntity = entity
            creditorId = "test-creditor-$name"
            validFrom = date
            createdBy = UUID_ZERO
        }
        val account = BankAccountEntity.new {
            userId = provider.organizationId
            iban = "test-iban"
            bic = "test-bic"
            accountHolder = name
            accountType = AccountType.CREDITOR
            createdBy = UUID_ZERO
        }
        val creditorId = CreditorIdentifierId(creditor.id.value.toString())
        val accountId = BankAccountId(account.id.value.toString())
        val collection = createSepaCollection(
            UUID_ZERO, creditorId, accountId, MandateReferencePrefix("TEST"), SepaCollectionKey(name),
            RemittanceInformation("Test"), StoredSequenceType.FRST, null
        )
        val mandate = createSepaMandateWithRetry(
            UUID_ZERO, creditor.id.value, account.id.value, "Test debtor", date, date, null,
            mandateReference = MandateReference("mandate-$name"), collectionId = collection.id.value
        )
        val payment = createPayment(
            UUID_ZERO, mandate.id.value, collection.id.value, 12.0,
            JodaLocalDate(executionDate.year, executionDate.monthNumber, executionDate.dayOfMonth)
        )
        return ProviderBanking(
            creditorId, accountId, SepaCollectionId(collection.id.value.toString()),
            SepaMandateId(mandate.id.value.toString()), SepaPaymentId(payment.id.value.toString())
        )
    }

    private data class SepaFixture(val providers: Providers, val source: ProviderBanking, val destination: ProviderBanking)

    private data class ProviderBanking(
        val creditorId: CreditorIdentifierId,
        val accountId: BankAccountId,
        val collectionId: SepaCollectionId,
        val mandateId: SepaMandateId,
        val paymentId: SepaPaymentId
    )

    private companion object {
        val executionDate = LocalDate(2026, 12, 1)
    }
}
