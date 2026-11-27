package org.solyton.solawi.bid.module.banking.routing

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.application.useApplicationTestDatabase
import org.solyton.solawi.bid.module.application.routing.SOURCE_USER
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.banking.schema.SepaCollectionMappings
import org.solyton.solawi.bid.module.banking.schema.SepaCollectionsTable
import org.solyton.solawi.bid.module.banking.schema.SepaMandateCollectionsTable
import org.solyton.solawi.bid.module.banking.schema.SepaMandateDataMappingsTable
import org.solyton.solawi.bid.module.banking.schema.SepaMandatesTable
import org.solyton.solawi.bid.module.banking.schema.SepaMessagesTable
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentLinksTable
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentsTable
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentStatusHistory
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentTemplatesTable
import org.solyton.solawi.bid.module.testFramework.getTestToken
import kotlin.test.assertEquals

internal class SepaWriteTenantRoutingTests {
    @Api
    @ParameterizedTest(name = "{0}")
    @MethodSource("mutations")
    fun crossTenantMutationIsRejectedWithoutChangingStoredSepaData(mutation: SepaMutation) = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val token = client.getTestToken(SOURCE_USER)
            val fixture = transaction(database) { seedBanking() }
            fun storedRows() = transaction(database) {
                sepaTables.associateWith { table ->
                    table.selectAll().map { row -> table.columns.map { column -> row[column] } }.toSet()
                }
            }
            val before = storedRows()

            val response = client.request("banking/sepa/${mutation.path}") {
                method = mutation.method
                bearerAuth(token)
                header(Header.CONTEXT, fixture.providers.source.contextId.toString())
                contentType(ContentType.Application.Json)
                setBody(mutation.body(fixture))
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals(before, storedRows())
        }
    }

    data class SepaMutation(
        val method: HttpMethod,
        val path: String,
        val scenario: String,
        val body: (SepaFixture) -> String
    ) {
        override fun toString() = "${method.value} $path ($scenario)"
    }

    companion object {
        private val sepaTables = listOf(
            SepaMandatesTable, SepaMandateCollectionsTable, SepaMandateDataMappingsTable,
            SepaCollectionsTable, SepaCollectionMappings, SepaPaymentsTable, SepaPaymentStatusHistory,
            SepaPaymentTemplatesTable, SepaPaymentLinksTable, SepaMessagesTable
        )

        private val signedAt = LocalDateTime(2026, 12, 1, 9, 0)

        @JvmStatic
        fun mutations(): List<SepaMutation> = listOf(
            mutation(HttpMethod.Post, "mandates/create", "foreign collection") { (_, _, destination) ->
                CreateSepaMandate(
                    creditorId = destination.creditorReference,
                    debtorBankAccountId = destination.debtorAccountId,
                    debtorName = "Test debtor",
                    mandateReference = MandateReference("additional-test-mandate"),
                    mandateReferencePrefix = null,
                    signedAt = signedAt,
                    validFrom = signedAt,
                    validUntil = null,
                    collectionId = destination.collectionId
                )
            },
            mutation(HttpMethod.Patch, "mandates/update", "foreign target creditor") { (_, source, destination) ->
                UpdateSepaMandate(
                    sepaMandateId = source.mandateId,
                    creditorId = destination.creditorReference,
                    debtorBankAccountId = source.debtorAccountId,
                    debtorName = "Updated test debtor",
                    mandateReference = MandateReference("updated-test-mandate"),
                    signedAt = signedAt,
                    validFrom = signedAt,
                    validUntil = null,
                    lastUsedAt = null
                )
            },
            mutation(HttpMethod.Post, "collections/create-payments", "foreign collection") { (_, _, destination) ->
                CreateSepaPaymentsForCollection(destination.collectionId, executionDate, mandateIds = listOf(destination.mandateId))
            },
            mutation(HttpMethod.Post, "collections/create-payment-successors", "mixed payment providers") { (_, source, destination) ->
                CreateSepaPaymentSuccessors(
                    executionDate, listOf(source.paymentId, destination.paymentId), SuccessorKind.NEXT_PERIOD
                )
            },
            mutation(HttpMethod.Post, "collections/generate-sepa-message", "foreign collection") { (_, _, destination) ->
                GenerateSepaMessageForCollection(destination.collectionId, executionDate, listOf(destination.paymentId))
            },
            mutation(HttpMethod.Patch, "collections/update", "foreign stored collection") { (_, source, destination) ->
                collectionUpdate(source).copy(sepaCollectionId = destination.collectionId)
            },
            mutation(HttpMethod.Patch, "collections/update", "foreign target account") { (_, source, destination) ->
                collectionUpdate(source).copy(creditorAccountId = destination.accountId)
            },
            mutation(HttpMethod.Post, "payments/create-ad-hoc", "foreign mandate") { (_, source, destination) ->
                CreateAdHocSepaPayment(
                    destination.mandateId, source.collectionId, 18.0, executionDate, PaymentExecutionStatus.CREATED
                )
            },
            mutation(HttpMethod.Post, "payments/create-ad-hoc", "foreign predecessor") { (_, source, destination) ->
                CreateAdHocSepaPayment(
                    source.mandateId, source.collectionId, 18.0, executionDate, PaymentExecutionStatus.CREATED, destination.paymentId
                )
            },
            mutation(HttpMethod.Patch, "payments/update", "foreign stored payment") { (_, _, destination) ->
                paymentUpdate(destination)
            },
            mutation(HttpMethod.Patch, "payments/update", "foreign target message") { (_, source, destination) ->
                paymentUpdate(source).copy(sepaMessageId = destination.messageId)
            },
            mutation(HttpMethod.Patch, "payments/update-many", "mixed payment providers") { (_, source, destination) ->
                UpdateSepaPayments(listOf(paymentUpdate(source), paymentUpdate(destination)))
            },
            mutation(HttpMethod.Delete, "payments/delete", "foreign stored payment") { (_, _, destination) ->
                DeleteSepaPayment(destination.paymentId)
            },
            mutation(HttpMethod.Delete, "payments/delete-many", "mixed payment providers") { (_, source, destination) ->
                DeleteSepaPayments(listOf(source.paymentId, destination.paymentId))
            },
            mutation(HttpMethod.Patch, "messages/update-status", "foreign stored message") { (_, _, destination) ->
                UpdateSepaMessageStatus(destination.messageId, SepaMessageStatus.SENT)
            }
        )

        private fun collectionUpdate(bank: ProviderBanking) = UpdateSepaCollection(
            bank.collectionId, bank.creditorId, bank.accountId, MandateReferencePrefix("TEST"),
            SepaCollectionKey("updated-collection"), RemittanceInformation("Test"), SepaSequenceType.FRST, null
        )

        private fun paymentUpdate(bank: ProviderBanking) = UpdateSepaPayment(
            bank.paymentId, bank.mandateId, bank.collectionId, 18.0, executionDate,
            SepaSequenceType.FRST, PaymentExecutionStatus.CREATED
        )

        private inline fun <reified T> mutation(
            method: HttpMethod,
            path: String,
            scenario: String,
            crossinline body: (SepaFixture) -> T
        ) = SepaMutation(method, path, scenario) { Json.encodeToString(body(it)) }
    }
}
