package org.solyton.solawi.bid.module.authentication

import com.typesafe.config.ConfigFactory
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.test.setup
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.Login
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthReceiveTest {

    @Api@Test
    fun authReceive() = runBlocking{
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/authentication.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }
            // login
            val response = login(username, password)
            assertTrue { response.status == HttpStatusCode.OK }

            val result = Json.decodeFromString(
                ResultSerializer,
                response.bodyAsText()
            )

            assertIs<Result.Success<LoggedIn>>(result)
            val accessToken = result.data.accessToken

            val testCallResponse = testCall(accessToken, "test-user-principle")
            assertTrue { testCallResponse.status == HttpStatusCode.OK }

            val testCallResult = Json.decodeFromString(
                ResultSerializer,
                testCallResponse.bodyAsText()
            )
            assertIs<Result.Success<Login>>(testCallResult)

            assertEquals(username, testCallResult.data.username)
        }
    }

    @Api@Test
    fun contextReceive() = runBlocking{
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/authentication.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }
            // login
            val response = login(username, password)
            assertTrue { response.status == HttpStatusCode.OK }

            val result = Json.decodeFromString(
                ResultSerializer,
                response.bodyAsText()
            )

            assertIs<Result.Success<LoggedIn>>(result)
            val accessToken = result.data.accessToken

            val testCallResponse = testCall(accessToken, "test-receive-contextual")
            assertTrue { testCallResponse.status == HttpStatusCode.OK }

            val testCallResult = Json.decodeFromString(
                ResultSerializer,
                testCallResponse.bodyAsText()
            )
            assertIs<Result.Success<Login>>(testCallResult)

            assertEquals(username, testCallResult.data.username)
        }
    }
}