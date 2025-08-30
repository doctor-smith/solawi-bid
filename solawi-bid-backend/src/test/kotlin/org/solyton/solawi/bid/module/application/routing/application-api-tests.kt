package org.solyton.solawi.bid.module.application.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.test.setup
import org.evoleq.test.setupData
import org.evoleq.uuid.UUID_ZERO
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.module.application.PASSWORD
import org.solyton.solawi.bid.module.application.USERNAME
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ApiUserApplications
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.ReadUserApplications
import org.solyton.solawi.bid.module.application.getRootContextByName
import org.solyton.solawi.bid.module.application.login
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.user.data.api.ApiUsers
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue


class ApplicationApiTests {
    @Api@Test
    fun readAllApplications() = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }

            // login
            val response = login(USERNAME, PASSWORD)
            assertTrue("failed to login") {
                response.status == HttpStatusCode.OK
            }

            val result = Json.decodeFromString(
                ResultSerializer,
                response.bodyAsText()
            )
            assertIs<Result.Success<LoggedIn>>(result, "login not successful")
            val accessToken = result.data.accessToken

            val context = getRootContextByName("APPLICATION", accessToken)

            val applicationsResponse = client.get("applications/all") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, context.id)
            }
            assertEquals(HttpStatusCode.OK, applicationsResponse.status, "HttpStatus not OK")
            val applicationsResponseText = applicationsResponse.bodyAsText()

            val applicationsResult = Json.decodeFromString(
                deserializer = ResultSerializer<ApiApplications>(),
                string = applicationsResponseText
            )
            assertIs<Result.Success<ApiApplications>>(applicationsResult)

            val applications = applicationsResult.data
            assertEquals(2, applications.list.size)
            assertEquals(4, applications.list.first().modules.size)
            assertEquals(5, applications.list[1].modules.size)
        }
    }
    @Api@Test
    fun cannotReadAllApplicationsWithoutToken() = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }



            val applicationsResponse = client.get("applications/all") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                //header(Header.CONTEXT, context.id)
            }
            assertEquals(HttpStatusCode.Unauthorized, applicationsResponse.status, "Wrong status")
        }
    }

    @Api@Test
    fun readPersonalApplications() = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }

            // login
            val response = login(USERNAME, PASSWORD)
            assertTrue("failed to login") {
                response.status == HttpStatusCode.OK
            }

            val result = Json.decodeFromString(
                ResultSerializer,
                response.bodyAsText()
            )
            assertIs<Result.Success<LoggedIn>>(result, "login not successful")
            val accessToken = result.data.accessToken

            val context = getRootContextByName("APPLICATION", accessToken)


            val applicationsResponse = client.get("applications/personal") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, context.id)
            }

            assertEquals(HttpStatusCode.OK, applicationsResponse.status, "HttpStatus not OK")
            val applicationsResponseText = applicationsResponse.bodyAsText()

            val applicationsResult = Json.decodeFromString(
                deserializer = ResultSerializer<ApiApplications>(),
                string = applicationsResponseText
            )
            assertIs<Result.Success<ApiApplications>>(applicationsResult)

            assertIs<Result.Success<ApiApplications>>(applicationsResult)

            val applications = applicationsResult.data.list
            assertEquals(1, applications.size)

            val modules = applications[0].modules
            assertEquals(4, modules.size)

            val registeredModules = modules.filter { it.lifecycleStage is LifecycleStage.Registered }
            val unregisteredModules = modules.filter { it.lifecycleStage is LifecycleStage.Empty }
            assertEquals(1, registeredModules.size)
            assertEquals(3, unregisteredModules.size)
        }
    }

    @Api@Test
    fun cannotReadPersonalApplicationsWithoutToken() = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }

            val applicationsResponse = client.get("applications/personal") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            assertEquals(HttpStatusCode.Unauthorized, applicationsResponse.status, "HttpStatus not OK")
        }
    }

    @Api@Test
    fun cannotReadPersonalApplicationsWithoutPermission() = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }

            val response = login("developer@alpha-structure.com", PASSWORD)
            assertTrue("failed to login") {
                response.status == HttpStatusCode.OK
            }

            val result = Json.decodeFromString(
                ResultSerializer,
                response.bodyAsText()
            )
            assertIs<Result.Success<LoggedIn>>(result, "login not successful")
            val accessToken = result.data.accessToken

            val context = getRootContextByName("APPLICATION", accessToken)


            val applicationsResponse = client.get("applications/personal") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, context.id)
            }


            assertEquals(HttpStatusCode.Forbidden, applicationsResponse.status, "HttpStatus not OK")
        }
    }

    @Api@Test
    fun cannotReadPersonalApplicationsInWrongContext() = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }

            val response = login("developer@solyton.org", PASSWORD)
            assertTrue("failed to login") {
                response.status == HttpStatusCode.OK
            }

            val result = Json.decodeFromString(
                ResultSerializer,
                response.bodyAsText()
            )
            assertIs<Result.Success<LoggedIn>>(result, "login not successful")
            val accessToken = result.data.accessToken

            val context = getRootContextByName("DUMMY_CONTEXT", accessToken)

            val applicationsResponse = client.get("applications/personal") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, context.id)
            }

            assertEquals(HttpStatusCode.Forbidden, applicationsResponse.status, "HttpStatus not OK")
        }
    }

    @Api@Test
    fun readApplicationsOfUsers() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val users: ApiUsers,

        )

        testApplication() {
            val data = setupData {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }

                // login
                val response = login(USERNAME, PASSWORD)
                assertTrue("failed to login") {
                    response.status == HttpStatusCode.OK
                }

                val result = Json.decodeFromString(
                    ResultSerializer,
                    response.bodyAsText()
                )
                assertIs<Result.Success<LoggedIn>>(result, "login not successful")
                val accessToken = result.data.accessToken

                val contextId = client.get("setup/application-module-context-id?app=APPLICATION_MANAGEMENT&module=APPLICATION_USER_MANAGEMENT").bodyAsText()

                val usersResponse = client.get("setup/dummy-users")
                val users = Json.decodeFromString<ApiUsers>(usersResponse.bodyAsText())

                Data(
                    contextId,
                    accessToken,
                    users
                )
            }

            val contextId = data.contextId
            val accessToken =  data.accessToken
            val users = data.users

            println("""Data:
                |contextId = $contextId
                |userIds = ${users.all.joinToString(", ") { it.id }}
            """.trimMargin())

            val applicationsResponse = client.patch("applications/management/users") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, contextId)
                setBody(
                    Json.encodeToString(
                        serializer = ReadUserApplications.serializer(),
                        value = ReadUserApplications(
                        users.all.map{it.id}
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.OK, applicationsResponse.status, "HttpStatus not OK")
            val applicationsResponseText = applicationsResponse.bodyAsText()

            val applicationsResult = Json.decodeFromString(
                deserializer = ResultSerializer<ApiUserApplications>(),
                string = applicationsResponseText
            )
            assertIs<Result.Success<ApiUserApplications>>(applicationsResult)


            val applications = applicationsResult.data.map
            // According to our data setup we expect 5 entries in the response
            assertEquals(5, applications.entries.size)
        }
    }

    @Api@Test
    fun cannotReadApplicationsOfUsersWithoutToken() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val users: ApiUsers,

            )

        testApplication() {
            val data = setupData {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }



                val contextId = client.get("setup/application-module-context-id?app=APPLICATION_MANAGEMENT&module=APPLICATION_USER_MANAGEMENT").bodyAsText()

                val usersResponse = client.get("setup/dummy-users")
                val users = Json.decodeFromString<ApiUsers>(usersResponse.bodyAsText())

                Data(
                    contextId,
                    UUID_ZERO.toString(),
                    users
                )
            }

            val contextId = data.contextId
            val accessToken =  data.accessToken
            val users = data.users



            val applicationsResponse = client.patch("applications/management/users") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, contextId)
                setBody(
                    Json.encodeToString(
                        serializer = ReadUserApplications.serializer(),
                        value = ReadUserApplications(
                            users.all.map{it.id}
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, applicationsResponse.status, "HttpStatus not Unauthorized")
        }
    }


    @Api@Test
    fun cannotReadApplicationsOfUsersWithoutPermission() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val users: ApiUsers,

            )

        testApplication() {
            val data = setupData {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }

                // login
                val response = login("dummy_0@solyton.org", PASSWORD)
                assertTrue("failed to login") {
                    response.status == HttpStatusCode.OK
                }

                val result = Json.decodeFromString(
                    ResultSerializer,
                    response.bodyAsText()
                )
                assertIs<Result.Success<LoggedIn>>(result, "login not successful")
                val accessToken = result.data.accessToken

                val contextId = client.get("setup/application-module-context-id?app=APPLICATION_MANAGEMENT&module=APPLICATION_USER_MANAGEMENT").bodyAsText()

                val usersResponse = client.get("setup/dummy-users")
                val users = Json.decodeFromString<ApiUsers>(usersResponse.bodyAsText())

                Data(
                    contextId,
                    accessToken,
                    users
                )
            }

            val contextId = data.contextId
            val accessToken =  data.accessToken
            val users = data.users

            println("""Data:
                |contextId = $contextId
                |userIds = ${users.all.joinToString(", ") { it.id }}
            """.trimMargin())

            val applicationsResponse = client.patch("applications/management/users") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, contextId)
                setBody(
                    Json.encodeToString(
                        serializer = ReadUserApplications.serializer(),
                        value = ReadUserApplications(
                            users.all.map{it.id}
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, applicationsResponse.status, "HttpStatus not Forbidden")
        }
    }

    @Api@Test
    fun cannotReadApplicationsOfUsersInWrongContext() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val users: ApiUsers,

            )

        testApplication() {
            val data = setupData {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }

                // login
                val response = login(USERNAME, PASSWORD)
                assertTrue("failed to login") {
                    response.status == HttpStatusCode.OK
                }

                val result = Json.decodeFromString(
                    ResultSerializer,
                    response.bodyAsText()
                )
                assertIs<Result.Success<LoggedIn>>(result, "login not successful")
                val accessToken = result.data.accessToken

                val contextId = getRootContextByName("DUMMY_CONTEXT", accessToken).id

                val usersResponse = client.get("setup/dummy-users")
                val users = Json.decodeFromString<ApiUsers>(usersResponse.bodyAsText())

                Data(
                    contextId,
                    accessToken,
                    users
                )
            }

            val contextId = data.contextId
            val accessToken =  data.accessToken
            val users = data.users

            println("""Data:
                |contextId = $contextId
                |userIds = ${users.all.joinToString(", ") { it.id }}
            """.trimMargin())

            val applicationsResponse = client.patch("applications/management/users") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, contextId)
                setBody(
                    Json.encodeToString(
                        serializer = ReadUserApplications.serializer(),
                        value = ReadUserApplications(
                            users.all.map{it.id}
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, applicationsResponse.status, "HttpStatus not Forbidden")
        }
    }
}
