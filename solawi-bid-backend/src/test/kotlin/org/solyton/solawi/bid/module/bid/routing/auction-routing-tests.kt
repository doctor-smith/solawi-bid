package org.solyton.solawi.bid.module.bid.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.kotlinx.date.todayWithTime
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultListSerializer
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.uuid.UUID_ZERO
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.testFramework.getTestToken
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuctionRoutingTests {

    @Api@Test
    fun createAuction() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }
            // get token
            val token = client.getTestToken("user@solyton.org")

            // create auction
            val response = client.post("/auction/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, "$UUID_ZERO")
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name", todayWithTime())
                    )
                )
            }

            assertTrue("Wrong status: ${response.status}, expected ${HttpStatusCode.OK}") { response.status == HttpStatusCode.OK }
        }
    }


    @Api@Test
    fun configureAuction() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }
            // get token
            val token = client.getTestToken("user@solyton.org")

            // create auction
            val response = client.post("/auction/create") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, "$UUID_ZERO")
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name", todayWithTime())
                    )
                )
            }
            assertTrue("Wrong status: ${response.status}, expected ${HttpStatusCode.OK}") { response.status == HttpStatusCode.OK }

            val auctionText = response.bodyAsText()
            val auctionResult = Json.decodeFromString(ResultSerializer, auctionText)
            assertIs<Result.Success<Auction>>(auctionResult)
            val auction = auctionResult.data

            // configure auction
            val configureAuctionResponse = client.patch("/auction/configure") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, "$UUID_ZERO")
                setBody(
                    Json.encodeToString(
                        ConfigureAuction.serializer(),
                        ConfigureAuction(
                            auction.id,
                            "test-name",
                            auction.date,
                            auctionDetails = AuctionDetails.SolawiTuebingen(
                                2.0, 2.0, 2.0, 2.0,
                            )
                        )
                    )
                )
            }

            // assertions
            assertTrue("Wrong status: ${configureAuctionResponse.status}, expected ${HttpStatusCode.OK}") { configureAuctionResponse.status == HttpStatusCode.OK }
        }
    }

    @Api@Test
    fun deleteAuction() {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }
            // get token
            val token = client.getTestToken("user@solyton.org")

            // create action
            val auctionText = client.post("/auction/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, "$UUID_ZERO")
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name", todayWithTime())
                    )
                )
            }.bodyAsText()
            val auctionResult = Json.decodeFromString(ResultSerializer, auctionText)
            assertIs<Result.Success<Auction>>(auctionResult)
            val auction = auctionResult.data

            // create another auction
            val auction1Text = client.post("/auction/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, "$UUID_ZERO")
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name-1", todayWithTime())
                    )
                )
            }.bodyAsText()
            val auction1Result = Json.decodeFromString(ResultSerializer, auction1Text)
            assertIs<Result.Success<Auction>>(auction1Result)

            // delete auction
            val response = client.delete("/auction/delete") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                header(Header.CONTEXT, "$UUID_ZERO")
                setBody(
                    Json.encodeToString(
                        DeleteAuctions.serializer(),
                        DeleteAuctions(listOf(auction.id))
                    )
                )
            }

            // assertions
            assertEquals(HttpStatusCode.OK, response.status)
            val result = Json.decodeFromString(ResultListSerializer<Auction>(), response.bodyAsText())
            assertIs<Result.Success<Auctions>>(result)

            val auctions = result.data

            assertTrue { auctions.list.isNotEmpty() }

            assertTrue { auctions.list.filter { it.name == "test-name-1" }.isNotEmpty() }
            assertTrue { auctions.list.filter { it.name == "test-name" }.isEmpty() }
        }
    }
}
