package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.patch
import io.ktor.client.request.header
import io.ktor.client.request.setBody
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
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.UpdateUser
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import java.io.File

class UserSelfUpdateRoutingTests {
    private val originalUsername = "developer@alpha-structure.com"
    private val originalPassword = "pass1234"

    @Api @Test
    fun userCannotUpdateAnotherAccountEvenWithCorrectOldPassword() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
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
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
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
}
