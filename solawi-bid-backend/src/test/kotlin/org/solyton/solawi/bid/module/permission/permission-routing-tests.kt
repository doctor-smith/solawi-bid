package org.solyton.solawi.bid.module.permission

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.permission.data.ContextId
import org.solyton.solawi.bid.module.permission.data.RightId
import org.solyton.solawi.bid.module.permission.data.RoleId
import org.solyton.solawi.bid.module.permission.data.api.Contexts
import org.solyton.solawi.bid.module.permission.data.api.PutRoleRightContext
import org.solyton.solawi.bid.module.permission.data.api.PutUserRoleContext
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUser
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.testFramework.getUserId
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PermissionRoutingTests {
    @Api@Test
    fun `user role context write requires manage permission in payload context`() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val attackerToken = client.getTestToken("unautorized@solyton.org")
            val targetToken = client.getTestToken("developer@alpha-structure.com")
            val targetUserId = client.getUserId("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val targetContextsResponse = client.patch("permissions/user/role-right-contexts") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $targetToken")
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        ReadRightRoleContextsOfUser.serializer(),
                        ReadRightRoleContextsOfUser(targetUserId)
                    )
                )
            }
            val targetContexts = Json.decodeFromString(
                ResultSerializer<Contexts>(),
                targetContextsResponse.bodyAsText()
            )
            assertIs<Result.Success<Contexts>>(targetContexts)
            val existingRoleIds = targetContexts.data.list
                .first { it.id == applicationContextId }
                .roles
                .map { it.id }

            val response = client.put("permissions/user/user-role-context") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $attackerToken")
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        PutUserRoleContext.serializer(),
                        PutUserRoleContext(targetUserId, applicationContextId, existingRoleIds)
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)

            val unchangedResponse = client.patch("permissions/user/role-right-contexts") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $targetToken")
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        ReadRightRoleContextsOfUser.serializer(),
                        ReadRightRoleContextsOfUser(targetUserId)
                    )
                )
            }
            val unchanged = Json.decodeFromString(
                ResultSerializer<Contexts>(),
                unchangedResponse.bodyAsText()
            )
            assertIs<Result.Success<Contexts>>(unchanged)
            assertEquals(
                existingRoleIds.toSet(),
                unchanged.data.list.first { it.id == applicationContextId }.roles.map { it.id }.toSet()
            )
        }
    }

    @Api@Test
    fun `role right context write requires manage permission in payload context`() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val attackerToken = client.getTestToken("unautorized@solyton.org")
            val targetToken = client.getTestToken("developer@alpha-structure.com")
            val targetUserId = client.getUserId("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val targetContextsResponse = client.patch("permissions/user/role-right-contexts") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $targetToken")
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        ReadRightRoleContextsOfUser.serializer(),
                        ReadRightRoleContextsOfUser(targetUserId)
                    )
                )
            }
            val targetContexts = Json.decodeFromString(
                ResultSerializer<Contexts>(),
                targetContextsResponse.bodyAsText()
            )
            assertIs<Result.Success<Contexts>>(targetContexts)
            val role = targetContexts.data.list
                .first { it.id == applicationContextId }
                .roles
                .first { it.rights.isNotEmpty() }

            val response = client.put("permissions/contexts/role-right-context") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $attackerToken")
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        PutRoleRightContext.serializer(),
                        PutRoleRightContext(
                            roleId = RoleId(role.id),
                            contextId = ContextId(applicationContextId),
                            rightIds = role.rights.map { RightId(it.id) }
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)

            val unchangedResponse = client.patch("permissions/user/role-right-contexts") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $targetToken")
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        ReadRightRoleContextsOfUser.serializer(),
                        ReadRightRoleContextsOfUser(targetUserId)
                    )
                )
            }
            val unchanged = Json.decodeFromString(
                ResultSerializer<Contexts>(),
                unchangedResponse.bodyAsText()
            )
            assertIs<Result.Success<Contexts>>(unchanged)
            val unchangedRole = unchanged.data.list
                .first { it.id == applicationContextId }
                .roles
                .first { it.id == role.id }
            assertEquals(role.rights.map { it.id }.toSet(), unchangedRole.rights.map { it.id }.toSet())
        }
    }

    @Api@Test
    fun `special context check uses the payload context before the request context`() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val token = client.getTestToken("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val response = client.post(
                "setup/permissions/special-context" +
                    "?special-context=$applicationContextId&derived-context=$applicationContextId"
            ) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, UUID.randomUUID().toString())
            }

            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Api@Test
    fun `derived context check ignores a manipulated request context`() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val token = client.getTestToken("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val response = client.post(
                "setup/permissions/derived-context" +
                    "?special-context=$applicationContextId&derived-context=$applicationContextId"
            ) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, UUID.randomUUID().toString())
            }

            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Api@Test
    fun `derived context check denies access even when request context is authorized`() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val token = client.getTestToken("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val deniedContextId = UUID.randomUUID().toString()
            val response = client.post(
                "setup/permissions/derived-context" +
                    "?special-context=$applicationContextId&derived-context=$deniedContextId"
            ) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, applicationContextId)
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Api@Test
    fun `authenticated user can read own roles and rights`() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val token = client.getTestToken("developer@alpha-structure.com")
            val userId = client.getUserId("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val response = client.patch("permissions/user/role-right-contexts") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        ReadRightRoleContextsOfUser.serializer(),
                        ReadRightRoleContextsOfUser(userId)
                    )
                )
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val result = Json.decodeFromString(
                ResultSerializer<Contexts>(),
                response.bodyAsText()
            )
            assertIs<Result.Success<Contexts>>(result)
            assertTrue(result.data.list.any { it.id == applicationContextId && it.name == "APPLICATION" })
        }
    }

    @Api@Test
    fun `user without permission cannot read another users roles and rights`() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val token = client.getTestToken("unautorized@solyton.org")
            val otherUserId = client.getUserId("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val response = client.patch("permissions/user/role-right-contexts") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        ReadRightRoleContextsOfUser.serializer(),
                        ReadRightRoleContextsOfUser(otherUserId)
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Api@Test
    fun `user roles and rights endpoint requires authentication`() = runBlocking {
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                )
            }

            val userId = client.getUserId("developer@alpha-structure.com")
            val applicationContextId = client.getTestContextIdByName("APPLICATION")
            val response = client.patch("permissions/user/role-right-contexts") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, applicationContextId)
                setBody(
                    Json.encodeToString(
                        ReadRightRoleContextsOfUser.serializer(),
                        ReadRightRoleContextsOfUser(userId)
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }
}
