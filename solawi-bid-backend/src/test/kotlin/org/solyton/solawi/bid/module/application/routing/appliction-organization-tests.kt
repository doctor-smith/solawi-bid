package org.solyton.solawi.bid.module.application.routing

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.test.setupData
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.USERNAME
import org.solyton.solawi.bid.module.application.application.useApplicationTestDatabase
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.application.getRootContextByName
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextsTable
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextsTable
import org.solyton.solawi.bid.module.testFramework.getTestToken
import java.util.*
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

        testApplication {
            val database = useApplicationTestDatabase()

            val data = setupData {
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

            assertEquals(HttpStatusCode.OK, registerApplicationsResponse.status)

            val registerApplicationsResult = Json.decodeFromString(
                ResultSerializer,
                registerApplicationsResponse.bodyAsText()
            )
            assertIs<org.evoleq.ktorx.result.Result.Success<ApiApplications>>(registerApplicationsResult)
            val registeredApplication = registerApplicationsResult.data.list.first { it.name == "AUCTIONS" }

            val organizationId = UUID.fromString(
                client.get("/setup/organization?name=OWN_ORGANIZATION&owner=$USERNAME").bodyAsText()
            )
            val foreignOrganizationId = UUID.fromString(
                client.get("/setup/organization?name=FOREIGN_ORGANIZATION&owner=dummy_0@solyton.org").bodyAsText()
            )
            val registeredModuleIds = registeredApplication.modules.filter { it.lifecycleStage is LifecycleStage.Registered }.map { it.id }

            fun linkedContexts(targetId: UUID) = transaction(database) {
                val applicationContexts = OrganizationApplicationContextsTable
                    .select(OrganizationApplicationContextsTable.contextId)
                    .where {
                        (OrganizationApplicationContextsTable.organizationId eq targetId) and
                            (OrganizationApplicationContextsTable.applicationId eq UUID.fromString(application.id))
                    }.map { it[OrganizationApplicationContextsTable.contextId].value }.toSet()
                val moduleContexts = OrganizationModuleContextsTable
                    .select(OrganizationModuleContextsTable.contextId)
                    .where {
                        (OrganizationModuleContextsTable.organizationId eq targetId) and
                            (OrganizationModuleContextsTable.moduleId inList registeredModuleIds.map(UUID::fromString))
                    }.map { it[OrganizationModuleContextsTable.contextId].value }.toSet()
                applicationContexts to moduleContexts
            }

            val foreignContextsBefore = linkedContexts(foreignOrganizationId)
            assertEquals(emptySet<UUID>() to emptySet<UUID>(), foreignContextsBefore)
            val forbiddenResponse = client.post("applications/personal/connect-organization") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${data.accessToken}")
                header(Header.CONTEXT, data.contextId)
                setBody(
                    Json.encodeToString(
                        ConnectApplicationToOrganization.serializer(),
                        ConnectApplicationToOrganization(
                            application.id,
                            foreignOrganizationId.toString(),
                            registeredModuleIds
                        )
                    )
                )
            }
            assertEquals(HttpStatusCode.Forbidden, forbiddenResponse.status)
            assertEquals(foreignContextsBefore, linkedContexts(foreignOrganizationId))

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

            val (applicationContexts, moduleContexts) = linkedContexts(organizationId)
            assertEquals(setOf(UUID.fromString(applicationOrganizationRelations.all.single().contextId)), applicationContexts)
            assertEquals(registeredModuleIds.size, moduleContexts.size)
            assertEquals(foreignContextsBefore, linkedContexts(foreignOrganizationId))
        }
    }
}
