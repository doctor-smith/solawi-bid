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
import org.evoleq.ktorx.result.ResultSerializer
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.bid.data.api.*
import java.io.File
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BidRoundEvaluationRoutingTest {

    @Api@Test
    fun exportBidRoundResultsTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // Create auction
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

            // Configure auction !
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


            // Create round
            val roundResponse = client.post("/round/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateRound.serializer(),
                        CreateRound(auction.id)
                    )
                )
            }
            assertTrue("Wrong status: ${roundResponse.status}, expected ${HttpStatusCode.OK}") { roundResponse.status == HttpStatusCode.OK }
            val roundResponseText = roundResponse.bodyAsText()
            val result = Json.decodeFromString(ResultSerializer<Round>(), roundResponseText)
            assertIs<Result.Success<Round>>(result)
            val baseRound = result.data


            // Evaluate
            val exportResultsResponse = client.patch("round/export-results") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        EvaluateBidRound.serializer(),
                        EvaluateBidRound(auction.id, baseRound.id)
                    )
                )
            }

            assertTrue("Wrong status: ${exportResultsResponse.status}, expected ${HttpStatusCode.OK}") { exportResultsResponse.status == HttpStatusCode.OK }
        }
    }


    @Api@Test
    fun evaluateBidRoundTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // Create auction
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

            // Configure auction !
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


            // Create round
            val roundResponse = client.post("/round/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateRound.serializer(),
                        CreateRound(auction.id)
                    )
                )
            }
            assertTrue("Wrong status: ${roundResponse.status}, expected ${HttpStatusCode.OK}") { roundResponse.status == HttpStatusCode.OK }
            val roundResponseText = roundResponse.bodyAsText()
            val result = Json.decodeFromString(ResultSerializer<Round>(), roundResponseText)
            assertIs<Result.Success<Round>>(result)
            val baseRound = result.data


            // Evaluate
            val evaluationResponse = client.patch("round/evaluate") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        EvaluateBidRound.serializer(),
                        EvaluateBidRound(auction.id, baseRound.id)
                    )
                )
            }

            assertTrue("Wrong status: ${evaluationResponse.status}, expected ${HttpStatusCode.OK}") { evaluationResponse.status == HttpStatusCode.OK }
        }
    }
    @Api@Test
    fun preEvaluateBidRoundTest() = runBlocking {
            testApplication {
                environment {
                    // Load the HOCON file explicitly with the file path
                    val configFile = File("src/test/resources/bid.api.test.conf")
                    config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

                }
                application {

                }
                // Create auction
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

                // Configure auction !
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

                // Create round
                val roundResponse = client.post("/round/create") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            CreateRound.serializer(),
                            CreateRound(auction.id)
                        )
                    )
                }
                assertTrue("Wrong status: ${roundResponse.status}, expected ${HttpStatusCode.OK}") { roundResponse.status == HttpStatusCode.OK }
                val roundResponseText = roundResponse.bodyAsText()
                val result = Json.decodeFromString(ResultSerializer<Round>(), roundResponseText)
                assertIs<Result.Success<Round>>(result)
                val baseRound = result.data


                // PreEvaluate
                val preEvaluationResponse = client.patch("round/pre-evaluate") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            PreEvaluateBidRound.serializer(),
                            PreEvaluateBidRound(auction.id, baseRound.id)
                        )
                    )
                }

                assertTrue("Wrong status: ${preEvaluationResponse.status}, expected ${HttpStatusCode.OK}") { preEvaluationResponse.status == HttpStatusCode.OK }
            }
        }



    @Api@Test
    fun acceptRoundTest() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            // Create auction
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

            // Configure auction !
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


            // Create round
            val roundResponse = client.post("/round/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateRound.serializer(),
                        CreateRound(auction.id)
                    )
                )
            }
            assertTrue("Wrong status: ${roundResponse.status}, expected ${HttpStatusCode.OK}") { roundResponse.status == HttpStatusCode.OK }
            val roundResponseText = roundResponse.bodyAsText()
            val result = Json.decodeFromString(ResultSerializer<Round>(), roundResponseText)
            assertIs<Result.Success<Round>>(result)
            val baseRound = result.data


            // Evaluate
            val exportResultsResponse = client.patch("round/export-results") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        EvaluateBidRound.serializer(),
                        EvaluateBidRound(auction.id, baseRound.id)
                    )
                )
            }

            assertTrue("Wrong status: ${exportResultsResponse.status}, expected ${HttpStatusCode.OK}") { exportResultsResponse.status == HttpStatusCode.OK }

            // Evaluate
            val acceptRoundResultsResponse = client.patch("auction/accept-round") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        AcceptRound.serializer(),
                        AcceptRound(auction.id, baseRound.id)
                    )
                )
            }

            assertTrue("Wrong status: ${acceptRoundResultsResponse.status}, expected ${HttpStatusCode.OK}") { acceptRoundResultsResponse.status == HttpStatusCode.OK }
        }
    }
}
