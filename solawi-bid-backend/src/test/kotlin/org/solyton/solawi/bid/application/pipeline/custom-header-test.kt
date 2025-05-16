package org.solyton.solawi.bid.application.pipeline

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.application.exception.ApplicationException
import org.solyton.solawi.bid.application.permission.Header
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class CustomHeaderTests {

    @Api@Test fun validateContextHeader() {
        testApplication {
            application {
                installSerializers()
            }

            routing {
                get("/validate") {
                    call.respondText(
                        text = Json.encodeToString(ResultSerializer(),Result.Success("Cool") as Result<String>),
                        status =  HttpStatusCode.OK
                    )
                }
            }

            val success = client.get("/validate") {
                header(Header.CONTEXT, "TEST_CONTEXT")
            }
            assertEquals(HttpStatusCode.OK, success.status)
            assertTrue{success.headers.contains(Header.CONTEXT, "TEST_CONTEXT")}

            val successResult = Json.decodeFromString(ResultSerializer, success.bodyAsText())
            assertTrue( successResult is Result.Success)
            assertEquals("Cool", successResult.data)


            val failure = client.get("/validate") {}
            assertEquals(HttpStatusCode.BadRequest, failure.status  )
            assertTrue{ failure.headers.contains(Header.CONTEXT, "EMPTY") }

            val failureResult = Json.decodeFromString(ResultSerializer, failure.bodyAsText())
            assertTrue( failureResult is Result.Failure.Message)
            assertEquals(ApplicationException.MissingContextHeader.message, failureResult.value)
        }
    }
}
