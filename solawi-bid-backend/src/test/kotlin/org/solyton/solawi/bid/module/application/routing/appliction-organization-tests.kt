package org.solyton.solawi.bid.module.application.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.test.setupData
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.module.application.USERNAME
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.ConnectApplicationToOrganization
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.RegisterForApplications
import org.solyton.solawi.bid.module.application.getRootContextByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ApplicationOrganizationTests {

    @Api@Test
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
                assertIs<org.evoleq.ktorx.result.Result.Success<ApiApplications>>(applicationsResult)

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
            assertIs<org.evoleq.ktorx.result.Result.Success<ApiApplications>>(registerApplicationsResult)
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
}
