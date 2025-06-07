package org.solyton.solawi.bid.module.authentication

import com.typesafe.config.ConfigFactory
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.test.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.authentication.data.api.AccessToken
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import java.io.File
import java.util.stream.Stream
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class AuthenticationRoutingTest {

    companion object {
        @JvmStatic
        fun testCases(): Stream<Arguments> = flatten(
            TestCases(group = "before-login", testCases = beforeLogin),
            TestCases(group = "after-login", testCases = afterLogin)
        ).map { Arguments.of(it.group, it.testCase) }.stream()

        val beforeLogin = arrayOf(
            "before-login",
            "login-with-wrong-username",
            "login-with-wrong-password",
        )
        val afterLogin = arrayOf(
            "login",
            "use-right-token",
            "use-wrong-token",
             "refresh-token",
             "use-old-token",
             "use-new-token",
             "revoke-token",
             "use-revoked-tokens",
            // "token-expiration" // ???,

        )
    }
    @Api@ParameterizedTest
    @MethodSource("testCases")
    fun login(group: String, case: String) = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/authentication.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
                application {
                    routing {
                        authenticate("auth-jwt") {
                            get("test") {
                                call.respond(HttpStatusCode.OK, Result.Success("OK"))
                            }
                        }
                    }
                }
            }

            test(group,case) {
                val rightUsername = "developer@alpha-structure.com"
                val rightPassword = "pass1234"

                testGroup("before-login"){
                    testCase("before-login") {
                        val beforeLoginResponse = testCall()
                        assertFalse("Status is OK") { beforeLoginResponse.status == HttpStatusCode.OK }
                        assertTrue("Status is not Unauthorized") { beforeLoginResponse.status == HttpStatusCode.Unauthorized }
                    }
                    testCase( "login-with-wrong-username"){
                        val loginResponse = login("unknown-user", rightPassword)
                        assertTrue("Status is OK but should not"){loginResponse.status != HttpStatusCode.OK}
                        assertTrue("Status in not Unauthorized"){loginResponse.status == HttpStatusCode.Unauthorized}
                    }
                    testCase("login-with-wrong-password"){
                        val loginResponse = login(rightUsername, "wrong-password")
                        assertTrue("Status is OK but should not"){loginResponse.status != HttpStatusCode.OK}
                        assertTrue("Status in not Unauthorized"){loginResponse.status == HttpStatusCode.Unauthorized}
                    }
                }

                testGroup("after-login") {
                    val loginResponse = login(rightUsername, rightPassword)
                    testCase("login") {
                        val x = loginResponse.bodyAsText()
                        assertTrue("Status not OK") { loginResponse.status == HttpStatusCode.OK }
                        assertTrue("Message has empty body") { x.isNotEmpty() }
                    }

                    // User is logged in and has a tokens :-)
                    val loggedInResult = Json.decodeFromString(ResultSerializer<LoggedIn>(), loginResponse.bodyAsText())
                    assertIs<Result.Success<LoggedIn>>(loggedInResult)

                    // Check access with and without token
                    val accessToken = loggedInResult.data.accessToken
                    val refreshToken = loggedInResult.data.refreshToken
                    var newToken: String? = null  // need to use statemonad

                    testCase( "use-wrong-token") {
                        val negativeResponse = testCall("1$accessToken")
                        assertFalse("Status is OK") { negativeResponse.status == HttpStatusCode.OK }
                        assertTrue("Status is not Unauthorized") { negativeResponse.status == HttpStatusCode.Unauthorized }
                    }
                    testCase( "use-right-token") {
                        val positiveResponse = testCall(accessToken)
                        assertTrue("Status not OK") { positiveResponse.status == HttpStatusCode.OK }
                    }
                    // Refresh token and check access with new token and old token
                    testCase("refresh-token"){
                        val refreshTokenResponse = refreshToken(rightUsername, refreshToken)
                        assertTrue("Status not OK") { refreshTokenResponse.status == HttpStatusCode.OK }
                        val result = Json.decodeFromString(
                            ResultSerializer,
                            refreshTokenResponse.bodyAsText()
                        )
                        assertIs<Result.Success<AccessToken>>(result)
                        newToken = result.data.accessToken
                        assertNotSame(accessToken, newToken)
                    }
                    // new token  -> expect success
                    testCase("use-new-token", true) {
                        val positiveResponse = testCall(newToken!!)
                        assertTrue("Status not OK") { positiveResponse.status == HttpStatusCode.OK }
                    }
                    // old token -> expect fail
                    testCase( "use-old-token", true) {
                        val negativeResponse = testCall(accessToken)
                        assertFalse("Status is OK") { negativeResponse.status == HttpStatusCode.OK }
                        assertTrue("Status is not Unauthorized") { negativeResponse.status == HttpStatusCode.Unauthorized }
                    }
                    // Revoke token and check access. Expect no token work anymore
                    testCase("revoke-token", true) {
                        kotlin.test.fail("Not implemented yet!")
                    }
                    testCase("use-revoked-tokens",true) {
                        kotlin.test.fail("Not implemented yet!")
                    }
                }
            }
        }
    }

}
