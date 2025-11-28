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
import org.solyton.solawi.bid.module.application.data.ConnectApplicationToOrganization
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ApiUserApplications
import org.solyton.solawi.bid.module.application.data.ApplicationContextRelations
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelation
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.ModuleContextRelations
import org.solyton.solawi.bid.module.application.data.ReadUserApplications
import org.solyton.solawi.bid.module.application.data.RegisterForApplications
import org.solyton.solawi.bid.module.application.data.StartTrialsOfApplications
import org.solyton.solawi.bid.module.application.getRootContextByName
import org.solyton.solawi.bid.module.application.login
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.ApiUsers
import java.io.File
import java.util.UUID
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
            val accessToken = client.getTestToken(USERNAME)
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
            val accessToken = client.getTestToken(USERNAME)

            val context = getRootContextByName("APPLICATION", accessToken)


            val applicationsResponse = client.get("applications/personal/all") {
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

            val applicationsResponse = client.get("applications/personal/all") {
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


            val applicationsResponse = client.get("applications/personal/all") {
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

            val accessToken = client.getTestToken(USERNAME)

            val context = getRootContextByName("DUMMY_CONTEXT", accessToken)

            val applicationsResponse = client.get("applications/personal/all") {
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

                // get token
                val accessToken = client.getTestToken(USERNAME)

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
                // get token
                val accessToken = client.getTestToken("dummy_0@solyton.org")

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

                // get token
                val accessToken = client.getTestToken(USERNAME)
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

    @Api@Test
    fun readPersonalApplicationContexts() = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }

            // get token
            val accessToken = client.getTestToken(USERNAME)

            val context = getRootContextByName("APPLICATION", accessToken)


            val applicationsResponse = client.get("applications/personal/application-context-relations") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, context.id)
            }

            assertEquals(HttpStatusCode.OK, applicationsResponse.status, "HttpStatus not OK")
            val applicationsResponseText = applicationsResponse.bodyAsText()

            val applicationsResult = Json.decodeFromString(
                deserializer = ResultSerializer<ApplicationContextRelations>(),
                string = applicationsResponseText
            )
            assertIs<Result.Success<ApplicationContextRelations>>(applicationsResult)

            // todo:dev improve tests
        }
    }

    @Api@Test
    fun readPersonalModuleContexts() = runBlocking {
        testApplication() {
            setup {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }
            }

            // get token
            val accessToken = client.getTestToken(USERNAME)

            val context = getRootContextByName("APPLICATION", accessToken)


            val applicationsResponse = client.get("applications/modules/personal/module-context-relations") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(Header.CONTEXT, context.id)
            }

            assertEquals(HttpStatusCode.OK, applicationsResponse.status, "HttpStatus not OK")
            val applicationsResponseText = applicationsResponse.bodyAsText()

            val applicationsResult = Json.decodeFromString(
                deserializer = ResultSerializer<ModuleContextRelations>(),
                string = applicationsResponseText
            )
            assertIs<Result.Success<ModuleContextRelations>>(applicationsResult)

            // todo:dev improve tests
        }
    }


    @Api@Test
    fun registerForApplications() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val applications: ApiApplications
        )

        testApplication() {
            val data = setupData {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }

                // get token
                val accessToken = client.getTestToken(USERNAME)

                val applicationContextId = getRootContextByName("APPLICATION", accessToken).id

                val applicationsResponse = client.get("applications/all") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header(Header.CONTEXT, applicationContextId)
                }
                val applicationsResult = Json.decodeFromString(
                    ResultSerializer,
                    applicationsResponse.bodyAsText()
                )
                assertIs<Result.Success<ApiApplications>>(applicationsResult)

                Data(
                    applicationContextId,
                    accessToken,
                    applicationsResult.data
                )
            }


            val application = data.applications.list.first { application -> application.name == "AUCTIONS" }

            val registerApplicationsResponse = client.patch("applications/personal/register") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${data.accessToken}")
                header(Header.CONTEXT, data.contextId)
                setBody(
                    Json.encodeToString(
                        serializer = RegisterForApplications.serializer(),
                        value = RegisterForApplications(
                            listOf(application.id)
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.OK, registerApplicationsResponse.status, "Wrong status")

            val registerApplicationsResult = Json.decodeFromString(
                ResultSerializer,
                registerApplicationsResponse.bodyAsText()
            )
            assertIs<Result.Success<ApiApplications>>(registerApplicationsResult)

            val apiApplications = registerApplicationsResult.data
            val auctionsApplication = apiApplications.list.first { it.name == "AUCTIONS" }
            assertIs<LifecycleStage.Registered>(auctionsApplication.lifecycleStage)
            val modules = auctionsApplication.modules
            val registeredModules = modules.filter { it.lifecycleStage is LifecycleStage.Registered }
            val unregisteredModules = modules.filterNot { it.lifecycleStage is LifecycleStage.Registered }

            assertEquals(
                setOf(
                    "AUCTION_MANAGEMENT",
                    "ORGANIZATION_MANAGEMENT"
                ),
                registeredModules.map { it.name }.toSet()
            )
            assertEquals(modules.size - 2, unregisteredModules.size)
        }
    }

    // @Api@Test
    fun connectApplicationToOrganizationTest() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val applications: ApiApplications
        )

        testApplication() {


            val data = setupData {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/application.module.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }

                // get token
                val accessToken = client.getTestToken(USERNAME)

                val applicationContextId = getRootContextByName("APPLICATION", accessToken).id

                val applicationsResponse = client.get("applications/all") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header(Header.CONTEXT, applicationContextId)
                }
                val applicationsResult = Json.decodeFromString(
                    ResultSerializer,
                    applicationsResponse.bodyAsText()
                )
                assertIs<Result.Success<ApiApplications>>(applicationsResult)

                Data(
                    applicationContextId,
                    accessToken,
                    applicationsResult.data
                )
            }


            val application = data.applications.list.first { application -> application.name == "AUCTIONS" }
            val x = client.get("/setup/registered-app?app-id=${application.id}")
            println(x)


            val registerApplicationsResponse = client.patch("applications/personal/register") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${data.accessToken}")
                header(Header.CONTEXT, data.contextId)
                setBody(
                    Json.encodeToString(
                        serializer = RegisterForApplications.serializer(),
                        value = RegisterForApplications(
                            listOf(application.id)
                        )
                    )
                )
            }

//            assertEquals(HttpStatusCode.OK, registerApplicationsResponse.status, "Wrong status during setup")

            val registerApplicationsResult = Json.decodeFromString(
                ResultSerializer,
                registerApplicationsResponse.bodyAsText()
            )
            assertIs<Result.Success<ApiApplications>>(registerApplicationsResult)
            val registeredApplication = registerApplicationsResult.data.list.first { it.name == "AUCTIONS" }

            val organizationId = UUID.randomUUID()
            val registeredModuleIds = registeredApplication.modules.filter { it.lifecycleStage is LifecycleStage.Registered }.map { it.id }
            val applicationOrganizationRelationsResponse = client.post("applications/personal/connect-organization") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${data.accessToken}")
                header(Header.CONTEXT, data.contextId)
                setBody(
                    Json.encodeToString(
                        serializer = ConnectApplicationToOrganization.serializer(),
                        value = ConnectApplicationToOrganization(
                            application.id,
                            organizationId.toString(),
                            registeredModuleIds
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.OK,applicationOrganizationRelationsResponse.status, "Wrong status code here")

            val applicationOrganizationRelationsResult = Json.decodeFromString(
                ResultSerializer,
                applicationOrganizationRelationsResponse.bodyAsText()
            )
            assertIs<Result.Success<ApplicationOrganizationRelations>>(applicationOrganizationRelationsResult)

            val applicationOrganizationRelations = applicationOrganizationRelationsResult.data
            assertEquals(1, applicationOrganizationRelations.all.size)

            assertEquals(2, applicationOrganizationRelations.all.first().moduleIds.size)

            val allModulesAreThere = applicationOrganizationRelations.all.first().moduleIds.containsAll(registeredModuleIds)
            assertTrue { allModulesAreThere }
        }
    }



    @Api@Test
    fun cannotRegisterForApplicationsTwice() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val applications: ApiApplications
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

                val applicationContextId = getRootContextByName("APPLICATION", accessToken).id

                val applicationsResponse = client.get("applications/all") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header(Header.CONTEXT, applicationContextId)
                }
                val applicationsResult = Json.decodeFromString(
                    ResultSerializer,
                    applicationsResponse.bodyAsText()
                )
                assertIs<Result.Success<ApiApplications>>(applicationsResult)

                Data(
                    applicationContextId,
                    accessToken,
                    applicationsResult.data
                )
            }


            val application = data.applications.list.first { application -> application.name == "AUCTIONS" }

            val registerApplicationsResponse = client.patch("applications/personal/register") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${data.accessToken}")
                header(Header.CONTEXT, data.contextId)
                setBody(
                    Json.encodeToString(
                        serializer = RegisterForApplications.serializer(),
                        value = RegisterForApplications(
                            listOf(application.id)
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.OK, registerApplicationsResponse.status, "Wrong status")

            val registerApplicationsResult = Json.decodeFromString(
                ResultSerializer,
                registerApplicationsResponse.bodyAsText()
            )
            assertIs<Result.Success<ApiApplications>>(registerApplicationsResult)

            val registerApplicationsTwiceResponse = client.patch("applications/personal/register") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${data.accessToken}")
                header(Header.CONTEXT, data.contextId)
                setBody(
                    Json.encodeToString(
                        serializer = RegisterForApplications.serializer(),
                        value = RegisterForApplications(
                            listOf(application.id)
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.Conflict, registerApplicationsTwiceResponse.status, "Wrong status")
        }
    }

    // todo:test Add more tests related to permissions and lifecycle management of apps and modules

    @Api@Test
    fun startTrialOfApplications() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val applications: ApiApplications
        )

        testApplication() {
            // Register AUCTIONS application
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

                val applicationContextId = getRootContextByName("APPLICATION", accessToken).id

                val applicationsResponse = client.get("applications/all") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header(Header.CONTEXT, applicationContextId)
                }
                val applicationsResult = Json.decodeFromString(
                    ResultSerializer,
                    applicationsResponse.bodyAsText()
                )
                assertIs<Result.Success<ApiApplications>>(applicationsResult)



                val application = applicationsResult.data.list.first { application -> application.name == "AUCTIONS" }

                val registerApplicationsResponse = client.patch("applications/personal/register") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header(Header.CONTEXT, applicationContextId)
                    setBody(
                        Json.encodeToString(
                            serializer = RegisterForApplications.serializer(),
                            value = RegisterForApplications(
                                listOf(application.id)
                            )
                        )
                    )
                }

                assertEquals(HttpStatusCode.OK, registerApplicationsResponse.status, "Wrong status")

                val registerApplicationsResult = Json.decodeFromString(
                    ResultSerializer,
                    registerApplicationsResponse.bodyAsText()
                )
                assertIs<Result.Success<ApiApplications>>(registerApplicationsResult)

                val apiApplications = registerApplicationsResult.data
                val auctionsApplication = apiApplications.list.first { it.name == "AUCTIONS" }
                assertIs<LifecycleStage.Registered>(auctionsApplication.lifecycleStage)

                Data(
                    applicationContextId,
                    accessToken,
                    applicationsResult.data
                )
            }

            val auctionsApplication = data.applications.list.first { it.name == "AUCTIONS" }

            val startTrialOfApplicationsResponse = client.patch("applications/personal/trial") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${data.accessToken}")
                header(Header.CONTEXT, data.contextId)
                setBody(
                    Json.encodeToString(
                        serializer = StartTrialsOfApplications.serializer(),
                        value = StartTrialsOfApplications(
                            listOf(auctionsApplication.id)
                        )
                    )
                )
            }

            assertEquals(HttpStatusCode.OK, startTrialOfApplicationsResponse.status, "Wrong status")


            val trialApplicationsResult = Json.decodeFromString(
                ResultSerializer,
                startTrialOfApplicationsResponse.bodyAsText()
            )
            assertIs<Result.Success<ApiApplications>>(trialApplicationsResult)

            val apiApplications = trialApplicationsResult.data
            val trialedApplication = apiApplications.list.first { it.name == "AUCTIONS" }
            assertIs<LifecycleStage.Trialing>(trialedApplication.lifecycleStage)
            val modules = trialedApplication.modules
            val trialedModules = modules.filter { it.lifecycleStage is LifecycleStage.Trialing }
            val untrialedModules = modules.filterNot { it.lifecycleStage is LifecycleStage.Trialing }

            assertEquals(
                setOf(
                    "AUCTION_MANAGEMENT",
                    "ORGANIZATION_MANAGEMENT"
                ),
                trialedModules.map { it.name }.toSet()
            )
            assertEquals(modules.size - 2, untrialedModules.size)
        }
    }


    @Api@Test
    fun subscribeApplications() = runBlocking {
        class Data(
            val contextId: String,
            val accessToken: String,
            val applications: ApiApplications
        )

        testApplication() {
            // Register AUCTIONS application
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

                val applicationContextId = getRootContextByName("APPLICATION", accessToken).id

                val applicationsResponse = client.get("applications/all") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header(Header.CONTEXT, applicationContextId)
                }
                val applicationsResult = Json.decodeFromString(
                    ResultSerializer,
                    applicationsResponse.bodyAsText()
                )
                assertIs<Result.Success<ApiApplications>>(applicationsResult)



                val application = applicationsResult.data.list.first { application -> application.name == "AUCTIONS" }

                val registerApplicationsResponse = client.patch("applications/personal/register") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header(Header.CONTEXT, applicationContextId)
                    setBody(
                        Json.encodeToString(
                            serializer = RegisterForApplications.serializer(),
                            value = RegisterForApplications(
                                listOf(application.id)
                            )
                        )
                    )
                }

                assertEquals(HttpStatusCode.OK, registerApplicationsResponse.status, "Wrong status")

                val registerApplicationsResult = Json.decodeFromString(
                    ResultSerializer,
                    registerApplicationsResponse.bodyAsText()
                )
                assertIs<Result.Success<ApiApplications>>(registerApplicationsResult)

                val startTrialOfApplicationsResponse = client.patch("applications/personal/trial") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer ${accessToken}")
                    header(Header.CONTEXT, applicationContextId)
                    setBody(
                        Json.encodeToString(
                            serializer = StartTrialsOfApplications.serializer(),
                            value = StartTrialsOfApplications(
                                listOf(application.id)
                            )
                        )
                    )
                }
                assertEquals(HttpStatusCode.OK, startTrialOfApplicationsResponse.status, "Wrong status")

                val startTrialOfApplicationsResult = Json.decodeFromString(
                    ResultSerializer,
                    startTrialOfApplicationsResponse.bodyAsText()
                )
                assertIs<Result.Success<ApiApplications>>(startTrialOfApplicationsResult)
                val apiApplications = startTrialOfApplicationsResult.data
                val auctionsApplication = apiApplications.list.first { it.name == "AUCTIONS" }
                assertIs<LifecycleStage.Trialing>(auctionsApplication.lifecycleStage)

                Data(
                    applicationContextId,
                    accessToken,
                    applicationsResult.data
                )
            }

            val auctionsApplication = data.applications.list.first { it.name == "AUCTIONS" }

            val subscribeApplicationsResponse = client.patch("applications/personal/subscribe") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${data.accessToken}")
                header(Header.CONTEXT, data.contextId)
                setBody(
                    Json.encodeToString(
                        serializer = StartTrialsOfApplications.serializer(),
                        value = StartTrialsOfApplications(
                            listOf(auctionsApplication.id)
                        )
                    )
                )
            }
            assertEquals(HttpStatusCode.OK, subscribeApplicationsResponse.status, "Wrong status")




            val subscribedApplicationsResult = Json.decodeFromString(
                ResultSerializer,
                subscribeApplicationsResponse.bodyAsText()
            )
            assertIs<Result.Success<ApiApplications>>(subscribedApplicationsResult)

            val apiApplications = subscribedApplicationsResult.data
            val subscribedApplication = apiApplications.list.first { it.name == "AUCTIONS" }
            assertIs<LifecycleStage.Active>(subscribedApplication.lifecycleStage)
            val modules = subscribedApplication.modules
            val trialedModules = modules.filter { it.lifecycleStage is LifecycleStage.Active }
            val untrialedModules = modules.filterNot { it.lifecycleStage is LifecycleStage.Active }

            assertEquals(
                setOf(
                    "AUCTION_MANAGEMENT",
                    "ORGANIZATION_MANAGEMENT"
                ),
                trialedModules.map { it.name }.toSet()
            )
            assertEquals(modules.size - 2, untrialedModules.size)
        }
    }
}
