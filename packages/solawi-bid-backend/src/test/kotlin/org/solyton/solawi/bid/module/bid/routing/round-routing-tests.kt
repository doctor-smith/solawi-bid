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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.bid.data.api.*
import java.io.File
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RoundRoutingTests {
    @Api@Test fun createRound() {
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

            val roundResponse  = client.post("/round/create") {
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

            assertEquals(RoundState.Opened.toString(), result.data.state)
        }
    }


    companion object {
        @JvmStatic
        fun testCases(): Stream<Arguments> = listOf(
            Arguments.of(1,  RoundState.Started),
            Arguments.of(2,  RoundState.Stopped),
            Arguments.of(3,  RoundState.Evaluated),
            Arguments.of(4,  RoundState.Closed),
            Arguments.of(5,  RoundState.Frozen),
            Arguments.of(6,  RoundState.Frozen),
        ).stream()
    }


    // todo improve
    @Api@ParameterizedTest
    @MethodSource("testCases")
    fun changeRouteStates(run: Int,  next: RoundState) = runBlocking {
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


            // Prepare run
            if(run > 1) {
                client.patch("round/change-state"){
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            ChangeRoundState.serializer(),
                            ChangeRoundState(baseRound.id, RoundState.Started.toString())
                        )
                    )
                }
            }
            if (run > 2) {
                client.patch("round/change-state") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            ChangeRoundState.serializer(),
                            ChangeRoundState(baseRound.id, RoundState.Stopped.toString())
                        )
                    )
                }
            }
            if (run > 3) {
                client.patch("round/change-state") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            ChangeRoundState.serializer(),
                            ChangeRoundState(baseRound.id, RoundState.Evaluated.toString())
                        )
                    )
                }
            }
            if (run > 4) {
                client.patch("round/change-state") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            ChangeRoundState.serializer(),
                            ChangeRoundState(baseRound.id, RoundState.Closed.toString())
                        )
                    )
                }
            }
            if (run > 5) {
                client.patch("round/change-state") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            ChangeRoundState.serializer(),
                            ChangeRoundState(baseRound.id, RoundState.Frozen.toString())
                        )
                    )
                }
            }

            // Need to handle failing cases first due to test structure
            roundStates.filter { it != next }.forEach {
                val changeRoundStateFailResponse = client.patch("round/change-state"){
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            ChangeRoundState.serializer(),
                            ChangeRoundState(baseRound.id, it.toString())
                        )
                    )
                }
                assertTrue("Wrong status: ${changeRoundStateFailResponse.status}, expected ${HttpStatusCode.BadRequest}") { changeRoundStateFailResponse.status == HttpStatusCode.BadRequest }

            }

            // now the positive case
            val changeRoundStateResponse = client.patch("round/change-state"){
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        ChangeRoundState.serializer(),
                        ChangeRoundState(baseRound.id, next.toString())
                    )
                )
            }
            assertTrue("Wrong status: ${changeRoundStateResponse.status}, expected ${HttpStatusCode.OK}") { changeRoundStateResponse.status == HttpStatusCode.OK }
        }
    }
}