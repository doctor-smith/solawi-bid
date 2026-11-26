package org.solyton.solawi.bid.module.banking.routing

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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.application.useApplicationTestDatabase
import org.solyton.solawi.bid.module.banking.data.RemittanceInformation
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.testFramework.getTestToken
import kotlin.test.assertEquals

class SepaEmptyBatchRoutingTests {
    @Api
    @ParameterizedTest(name = "{0}")
    @MethodSource("emptyBatches")
    fun emptySepaBatchReturnsBadRequest(batch: EmptyBatch) = runBlocking {
        testApplication {
            useApplicationTestDatabase()
            val token = client.getTestToken("dummy_0@solyton.org")

            val response = client.request("banking/sepa/${batch.path}") {
                method = batch.method
                bearerAuth(token)
                header(Header.CONTEXT, UUID_ZERO.toString())
                contentType(ContentType.Application.Json)
                setBody(batch.body)
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            val failure = Json.decodeFromString(Result.Failure.Message.serializer(), response.bodyAsText())
            assertEquals(Result.Failure.Message(batch.message), failure)
        }
    }

    data class EmptyBatch(val path: String, val method: HttpMethod, val body: String, val message: String) {
        override fun toString() = "${method.value} $path"
    }

    companion object {
        @JvmStatic
        fun emptyBatches(): List<EmptyBatch> {
            val date = LocalDate(2026, 12, 1)
            return listOf(
                paymentBatch(
                    "collections/create-payment-successors", HttpMethod.Post,
                    CreateSepaPaymentSuccessors(date, emptyList(), SuccessorKind.NEXT_PERIOD)
                ),
                paymentBatch(
                    "collections/generate-sepa-message", HttpMethod.Post,
                    GenerateSepaMessageForCollection(SepaCollectionId(UUID_ZERO.toString()), date, emptyList())
                ),
                paymentBatch(
                    "payments/update-execution-statuses", HttpMethod.Patch,
                    UpdateSepaPaymentExecutionStatuses(PaymentExecutionStatus.CREATED, emptyList())
                ),
                paymentBatch("payments/update-many", HttpMethod.Patch, UpdateSepaPayments(emptyList())),
                paymentBatch("payments/delete-many", HttpMethod.Delete, DeleteSepaPayments(emptyList())),
                EmptyBatch(
                    "messages/merge", HttpMethod.Post,
                    Json.encodeToString(MergeSepaMessages(emptyList(), date, RemittanceInformation("Test"))),
                    "At least one SEPA message is required"
                )
            )
        }

        private inline fun <reified T> paymentBatch(path: String, method: HttpMethod, body: T) =
            EmptyBatch(path, method, Json.encodeToString(body), "At least one SEPA payment is required")
    }
}
