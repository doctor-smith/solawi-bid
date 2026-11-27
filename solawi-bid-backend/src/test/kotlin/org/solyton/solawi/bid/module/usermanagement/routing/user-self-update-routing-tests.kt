package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.patch
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.authentication.login
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.UpdateUser
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import java.io.File
import java.util.UUID

class UserSelfUpdateRoutingTests {
    private val originalUsername = "developer@alpha-structure.com"
    private val originalPassword = "pass1234"

    private fun isolatedConfig() = HoconApplicationConfig(
        ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
            .withValue("database.url", ConfigValueFactory.fromAnyRef("jdbc:h2:mem:self_update_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"))
    )

    private fun updateRequest() = UpdateUser(
        oldUsername = Username(originalUsername),
        newUsername = Username("updated-self@solyton.org"),
        oldPassword = Password(originalPassword),
        newPassword = Password("new-password-123")
    )

    private suspend fun HttpClient.update(token: String, context: String, request: UpdateUser) = patch("users/update") {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(Header.CONTEXT, context)
        setBody(Json.encodeToString(UpdateUser.serializer(), request))
    }

    @Api @Test
    fun userCannotUpdateAnotherAccountEvenWithCorrectOldPassword() = runBlocking {
        testApplication {
            environment {
                config = isolatedConfig()
            }

            val otherUserToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val request = UpdateUser(
                oldUsername = Username(originalUsername),
                newUsername = Username("unexpected-foreign-account-rename@solyton.org"),
                oldPassword = Password(originalPassword),
                newPassword = Password(originalPassword)
            )

            val response = client.patch("users/update") {
                header(HttpHeaders.Authorization, "Bearer $otherUserToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(UpdateUser.serializer(), request))
            }
            assertEquals(HttpStatusCode.Forbidden, response.status)

            // Read the original identity again: the failed request must not mutate it.
            val originalUserToken = client.getTestToken(originalUsername)
            val positiveResponse = client.patch("users/update") {
                header(HttpHeaders.Authorization, "Bearer $originalUserToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    UpdateUser.serializer(),
                    request.copy(newUsername = Username(originalUsername))
                ))
            }
            assertEquals(HttpStatusCode.OK, positiveResponse.status)
        }
    }

    @Api @Test
    fun userUpdateRequiresAuthentication() = runBlocking {
        testApplication {
            environment {
                config = isolatedConfig()
            }
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val response = client.patch("users/update") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    UpdateUser.serializer(),
                    UpdateUser(
                        oldUsername = Username(originalUsername),
                        newUsername = Username(originalUsername),
                        oldPassword = Password(originalPassword),
                        newPassword = Password(originalPassword)
                    )
                ))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Api @Test
    fun userCanChangeOwnUsernameAndPassword() = runBlocking {
        testApplication {
            environment { config = isolatedConfig() }
            val token = client.getTestToken(originalUsername)
            val context = client.getTestContextIdByName("APPLICATION")
            val request = updateRequest()

            assertEquals(HttpStatusCode.OK, client.update(token, context, request).status)
            assertEquals(HttpStatusCode.OK, login(request.newUsername.value, request.newPassword.value).status)
            assertEquals(HttpStatusCode.Unauthorized, login(originalUsername, originalPassword).status)
            assertEquals(HttpStatusCode.Unauthorized, login(request.newUsername.value, originalPassword).status)
        }
    }

    @Api @Test
    fun wrongPasswordDoesNotChangeUsernameOrPassword() = runBlocking {
        testApplication {
            environment { config = isolatedConfig() }
            val token = client.getTestToken(originalUsername)
            val context = client.getTestContextIdByName("APPLICATION")
            val request = updateRequest()

            assertEquals(HttpStatusCode.Unauthorized, client.update(token, context, request.copy(oldPassword = Password("wrong-password"))).status)
            assertEquals(HttpStatusCode.OK, login(originalUsername, originalPassword).status)
            assertEquals(HttpStatusCode.Unauthorized, login(originalUsername, request.newPassword.value).status)
            assertEquals(HttpStatusCode.Unauthorized, login(request.newUsername.value, request.newPassword.value).status)
        }
    }

    @Api @Test
    fun invalidJwtIsRejected() = runBlocking {
        testApplication {
            environment { config = isolatedConfig() }
            val token = client.getTestToken(originalUsername)
            val context = client.getTestContextIdByName("APPLICATION")
            assertEquals(HttpStatusCode.Unauthorized, client.update("1$token", context, updateRequest()).status)
            assertEquals(HttpStatusCode.OK, login(originalUsername, originalPassword).status)
        }
    }

    @Api @Test
    fun foreignAndUnknownAccountsHaveTheSameError() = runBlocking {
        testApplication {
            environment { config = isolatedConfig() }
            val token = client.getTestToken("unautorized@solyton.org")
            val context = client.getTestContextIdByName("APPLICATION")
            val request = updateRequest()
            val foreign = client.update(token, context, request)
            val unknown = client.update(token, context, request.copy(oldUsername = Username("unknown-account@solyton.org")))

            assertEquals(HttpStatusCode.Forbidden, foreign.status)
            assertEquals(foreign.status, unknown.status)
            assertEquals(foreign.bodyAsText(), unknown.bodyAsText())
            assertEquals(HttpStatusCode.OK, login(originalUsername, originalPassword).status)
        }
    }
}
