package org.solyton.solawi.bid.module.bid.routing

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
import org.evoleq.test.setupData
import org.evoleq.uuid.UUID_ZERO
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.routing.util.getApplications
import org.solyton.solawi.bid.module.testFramework.*
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

const val OWNER = "owner@solyton.org"

class BidApplicationRoutingTest {

    @Api@Test
    fun connectApplicationToOrganizationAndCheckAccessRightsTest() = runBlocking {
        class Data(
            val auctionApplicationContextId: String,
            val applicationsContextId: String,
            val accessToken: String,
            val applications: ApiApplications
        )



        testApplication() {
            val data = setupData {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/bid.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))
                }

                // get token
                val accessToken = client.getTestToken(OWNER)

                val auctionApplicationContextId = client.getTestContextIdByName("AUCTIONS")

                val applicationsResult = client.getApplications(
                    accessToken,
                    auctionApplicationContextId
                )
                Data(
                    auctionApplicationContextId,
                    client.getTestContextIdByName("APPLICATION"),
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
                header(Header.CONTEXT, data.applicationsContextId)
                setBody(
                    Json.encodeToString(
                        serializer = RegisterForApplications.serializer(),
                        value = RegisterForApplications(
                            listOf(application.id)
                        )
                    )
                )
            }

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
                header(Header.CONTEXT, data.auctionApplicationContextId)
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

            val userId = client.getUserId(OWNER)

            val contextIdFromRelations = applicationOrganizationRelations.all[0].contextId
            val rightId = client.getTestRightIdByName("CREATE_AUCTION")

            val isGranted = client.isGranted(userId, contextIdFromRelations, rightId)
            assertTrue(isGranted, "User should have the right to create auctions 1")
            val isGrantedInAuctionsContext = client.isGranted(userId, data.auctionApplicationContextId, rightId)
            assertTrue(isGrantedInAuctionsContext,"User should have the right to create auctions 2")

            // assertTrue { contextIdFromRelations == data.contextId }

            val isGrantedInApplicationContext = client.isGranted(userId, data.applicationsContextId, rightId)
            assertFalse(isGrantedInApplicationContext)

            /* Debug
            val rrc1 = client.getRoleRightContextsAsMdByContextId(contextId)
            println(rrc1)
            val rrc2 = client.getRoleRightContextsAsMdByContextId(data.applicationsContextId)
            println(rrc2)

            val md = client.getUsersRoleRightContextsAsMd(userId)

            println("md = $md")
            val contextsMd = client.getContextsAsMd()
            println(contextsMd)
            */

            // try to create an auction
            val createAuctionResult = client.createAuction(
                "Test Auction ",
                data.accessToken,
                data.auctionApplicationContextId,
                contextIdFromRelations,
            )

            assertIs<Result.Success<ApiAuction>>(createAuctionResult)

            // try to create an auction
            val createAuctionResult2 = client.createAuction(
                "Test Auction 2 ",
                data.accessToken,
                data.auctionApplicationContextId,
                data.auctionApplicationContextId,
            )

            assertIs<Result.Success<ApiAuction>>(createAuctionResult2)

            // try to create an auction
            val createAuctionResult3 = client.createAuction(
                "Test Auction 3 ",
                data.accessToken,
                data.applicationsContextId,
                data.auctionApplicationContextId,
            )

            assertIs<Result.Success<ApiAuction>>(createAuctionResult3)

            // try to create an auction
            val createAuctionResult4 = client.createAuction(
                "Test Auction 4 ",
                data.accessToken,
                data.applicationsContextId,
                contextIdFromRelations,
            )

            assertIs<Result.Success<ApiAuction>>(createAuctionResult4)

            // try to create an auction
            val createAuctionResult5 = client.createAuction(
                "Test Auction 5 ",
                data.accessToken,
                UUID_ZERO.toString(),
                contextIdFromRelations,
            )

            assertIs<Result.Success<ApiAuction>>(createAuctionResult5)

            // try to create an auction
            val createAuctionResult6 = client.createAuction(
                "Test Auction 6 ",
                data.accessToken,
                UUID_ZERO.toString(),
                data.auctionApplicationContextId,
            )

            assertIs<Result.Success<ApiAuction>>(createAuctionResult6)

            // try to create an auction
            val createAuctionResult7 = client.createAuction(
                "Test Auction 7 ",
                data.accessToken,
                UUID_ZERO.toString(),
                data.applicationsContextId,
            )

            assertIs<Result.Failure>(createAuctionResult7)

            // try to create an auction
            val createAuctionResult8 = client.createAuction(
                "Test Auction 8 ",
                data.accessToken,
                data.applicationsContextId,
                UUID_ZERO.toString(),
            )

            assertIs<Result.Failure>(createAuctionResult8)
        }
    }
}
