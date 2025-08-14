package org.solyton.solawi.bid.application.pipeline

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.test.setup
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.application.exception.ApplicationException
import org.solyton.solawi.bid.application.permission.Header
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomHeaderTests {

    @Api@Test
    fun validateContextHeader() = runBlocking{
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }
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
            assertEquals(HttpStatusCode.OK, success.status, "Fuck! Status is not OK")
            println(success.headers)
            assertTrue("Step 2"){success.headers.contains(Header.CONTEXT, "TEST_CONTEXT")}

            val successResult = Json.decodeFromString(ResultSerializer, success.bodyAsText())
            assertTrue( successResult is Result.Success, "No success")
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
