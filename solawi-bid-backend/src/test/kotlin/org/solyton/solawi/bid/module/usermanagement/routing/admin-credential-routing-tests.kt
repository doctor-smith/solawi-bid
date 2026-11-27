package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.header
import io.ktor.client.request.patch
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
import org.solyton.solawi.bid.module.user.data.api.ChangePassword
import org.solyton.solawi.bid.module.user.data.api.UpdateUser
import org.solyton.solawi.bid.module.user.data.api.UpdateUsername
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import java.io.File

class AdminCredentialRoutingTests {
    @Api @Test
    fun nonAdministratorCannotResetAnotherUsersPassword() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val userToken = client.getTestToken("unautorized@solyton.org")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val forbidden = client.patch("users/change-password") {
                header(HttpHeaders.Authorization, "Bearer $userToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    ChangePassword.serializer(),
                    ChangePassword("developer@alpha-structure.com", "different-test-password")
                ))
            }
            assertEquals(HttpStatusCode.Forbidden, forbidden.status)

            // A denied reset must leave the target account's old credentials intact.
            val ownerToken = client.getTestToken("developer@alpha-structure.com")
            val unchanged = client.patch("users/update") {
                header(HttpHeaders.Authorization, "Bearer $ownerToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    UpdateUser.serializer(),
                    UpdateUser(
                        oldUsername = Username("developer@alpha-structure.com"),
                        newUsername = Username("developer@alpha-structure.com"),
                        oldPassword = Password("pass1234"),
                        newPassword = Password("pass1234")
                    )
                ))
            }
            assertEquals(HttpStatusCode.OK, unchanged.status)
        }
    }

    @Api @Test
    fun nonAdministratorCannotRenameAnotherAccount() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }
            val callerToken = client.getTestToken("unautorized@solyton.org")
            val targetName = "developer@alpha-structure.com"
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val forbidden = client.patch("users/update-username") {
                header(HttpHeaders.Authorization, "Bearer $callerToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    UpdateUsername.serializer(),
                    UpdateUsername(
                        oldUsername = Username(targetName),
                        newUsername = Username("not-an-admin-rename@solyton.org")
                    )
                ))
            }
            assertEquals(HttpStatusCode.Forbidden, forbidden.status)

            // The denied request must not rename the actual account.
            val originalUserToken = client.getTestToken(targetName)
            val unchanged = client.patch("users/update") {
                header(HttpHeaders.Authorization, "Bearer $originalUserToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    UpdateUser.serializer(),
                    UpdateUser(
                        oldUsername = Username(targetName),
                        newUsername = Username(targetName),
                        oldPassword = Password("pass1234"),
                        newPassword = Password("pass1234")
                    )
                ))
            }
            assertEquals(HttpStatusCode.OK, unchanged.status)
        }
    }

    @Api @Test
    fun passwordResetRequiresAuthentication() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val response = client.patch("users/change-password") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    ChangePassword.serializer(),
                    ChangePassword("developer@alpha-structure.com", "unused-test-password")
                ))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Api @Test
    fun passwordPolicyAppliesToPersonalPasswordChanges() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }
            val username = "developer@alpha-structure.com"
            val token = client.getTestToken(username)
            val applicationContextId = client.getTestContextIdByName("APPLICATION")

            val rejected = client.patch("users/update") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    UpdateUser.serializer(),
                    UpdateUser(
                        oldUsername = Username(username),
                        newUsername = Username(username),
                        oldPassword = Password("pass1234"),
                        newPassword = Password("too-short")
                    )
                ))
            }
            assertEquals(HttpStatusCode.BadRequest, rejected.status)

            val unchanged = client.patch("users/update") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(Json.encodeToString(
                    UpdateUser.serializer(),
                    UpdateUser(
                        oldUsername = Username(username),
                        newUsername = Username(username),
                        oldPassword = Password("pass1234"),
                        newPassword = Password("pass1234")
                    )
                ))
            }
            assertEquals(HttpStatusCode.OK, unchanged.status)
        }
    }
}
