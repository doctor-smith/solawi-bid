package org.solyton.solawi.bid.module.bid.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultListSerializer
import org.evoleq.ktorx.result.ResultSerializer
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.bid.data.api.*
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuctionRoutingTests {

    @Api
    @Test
    fun createAuction() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }
            val response = client.post("/auction/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name", LocalDate(1, 1, 1))
                    )
                )
            }

            assertTrue("Wrong status: ${response.status}, expected ${HttpStatusCode.OK}") { response.status == HttpStatusCode.OK }
        }
    }


    @Api
    @Test
    fun configureAuction() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }
            val response = client.post("/auction/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name", LocalDate(1, 1, 1))
                    )
                )
            }
            assertTrue("Wrong status: ${response.status}, expected ${HttpStatusCode.OK}") { response.status == HttpStatusCode.OK }

            val auctionText = response.bodyAsText()
            val auctionResult = Json.decodeFromString(ResultSerializer, auctionText)
            assertIs<Result.Success<Auction>>(auctionResult)
            val auction = auctionResult.data

            val configureAuctionResponse = client.patch("/auction/configure") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
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
            val auctionText = client.post("/auction/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name", LocalDate(1, 1, 1))
                    )
                )
            }.bodyAsText()
            val auctionResult = Json.decodeFromString(ResultSerializer, auctionText)
            assertIs<Result.Success<Auction>>(auctionResult)
            val auction = auctionResult.data


            val auction1Text = client.post("/auction/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name-1", LocalDate(1, 1, 1))
                    )
                )
            }.bodyAsText()
            val auction1Result = Json.decodeFromString(ResultSerializer, auction1Text)
            assertIs<Result.Success<Auction>>(auction1Result)
            val auction1 = auctionResult.data

            val response = client.delete("/auction/delete") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        DeleteAuctions.serializer(),
                        DeleteAuctions(listOf(auction.id))
                    )
                )
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val result = Json.decodeFromString(ResultListSerializer<Auction>(), response.bodyAsText())
            assertIs<Result.Success<Auctions>>(result)

            val auctions = result.data

            println(auctions.list.map { it.name })
            assertTrue { auctions.list.isNotEmpty() }

            assertTrue { auctions.list.filter { it.name == "test-name-1" }.isNotEmpty() }
            assertTrue { auctions.list.filter { it.name == "test-name" }.isEmpty() }
        }
    }
}
