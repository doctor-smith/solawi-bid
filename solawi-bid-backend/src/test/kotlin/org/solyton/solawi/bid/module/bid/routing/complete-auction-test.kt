package org.solyton.solawi.bid.module.bid.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.*
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
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CompleteAuctionTest {

    @Api
    @Test
    fun completeAuction() = runBlocking{
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application {

            }

            val auctionResult = client.createAuction("test-auction")
            assertIs<Result.Success<ApiAuction>>(auctionResult)
            val auction = auctionResult.data
            val auctionId = auction.id

            // configure auction
            val auctionDetails = AuctionDetails.SolawiTuebingen(
                0.0,
                80.0,
                600.0,
                20.0
            )
            val configuredAuction = client.configureAuction(ConfigureAuction(
                auctionId,
                auction.name,
                auction.date,
                auctionDetails
            ))
            assertIs<Result.Success<ApiAuction>>(configuredAuction)
            // import bidders
            val importBidders = ImportBidders(
                auctionId = auctionId,
                listOf(
                    NewBidder("bidder_1@auction.com", 0,1),
                    NewBidder("bidder_2@auction.com", 0,1),
                    NewBidder("bidder_3@auction.com", 0,1),
                    NewBidder("bidder_4@auction.com", 0,1),
                    NewBidder("bidder_5@auction.com", 0,1),
                    NewBidder("bidder_6@auction.com", 0,2)

                )
            )
            val auctionWithBiddersResult = client.importBidders(importBidders)
            assertIs<Result.Success<ApiAuction>>(auctionWithBiddersResult)

            // create round
            val roundResult = client.createRound(auctionId)
            assertIs<Result.Success<ApiRound>>(roundResult)

            val round = roundResult.data
            val link = round.link

            // start round
            val startedRound = client.changeRoundState(ChangeRoundState(round.id, RoundState.Started.toString()))
            assertIs<Result.Success<ApiAuction>>(startedRound)

            // bid
            val bid1Result = client.sendBid(Bid("bidder_1@auction.com", link, 100.0))
            assertIs<Result.Success<ApiBidRound>>(bid1Result)

            val bid2Result = client.sendBid(Bid("bidder_2@auction.com", link, 200.0))
            assertIs<Result.Success<ApiBidRound>>(bid2Result)


            // stop round

            // check results
            val results1Result = client.exportRoundResults(ExportBidRound(round.id, auctionId))
            assertIs<Result.Success<ApiBidRoundResults>>(results1Result)
            val results1 = results1Result.data
            println("""
                |
                |Results = $results1
                |
            """.trimMargin())

            val expected1: List<BidResult> = listOf(
                BidResult("bidder_1@auction.com",1,100.0,true ),
                BidResult("bidder_2@auction.com",1,200.0,true ),
                BidResult("bidder_3@auction.com",1,100.0,false ),
                BidResult("bidder_4@auction.com",1,100.0,false ),
                BidResult("bidder_5@auction.com",1,100.0,false ),
                BidResult("bidder_6@auction.com",2,100.0,false )
            )
            val bidResults1 = results1Result.data.results
            assertEquals(expected1, bidResults1)

            // check evaluation
            val expectedEvaluation1 = ApiBidRoundEvaluation(
                auctionDetails,
                12.0 * 800.0,
                7,
                listOf(
                    WeightedBid(1,100.0),
                    WeightedBid(1,200.0),
                    WeightedBid(1,100.0),
                    WeightedBid(1,100.0),
                    WeightedBid(1,100.0),
                    WeightedBid(2,100.0),
                )
            )

            val evaluation1Result = client.evaluateRound(EvaluateBidRound(auctionId,round.id))
            assertIs<Result.Success<ApiBidRoundEvaluation>>(evaluation1Result)
            val evaluation1 = evaluation1Result.data
            assertEquals(expectedEvaluation1, evaluation1)

        }
    }
}

suspend fun HttpClient.createAuction(name: String): Result<ApiAuction> {
    val createAuctionText = post("/auction/create") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
            Json.encodeToString(
                CreateAuction.serializer(),
                CreateAuction(name, LocalDate(1, 1, 1))
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiAuction>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}

suspend fun HttpClient.configureAuction(configureAuction: ConfigureAuction): Result<ApiAuction> {
    val createAuctionText = patch("/auction/configure") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
            Json.encodeToString(
                ConfigureAuction.serializer(),
                configureAuction
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiAuction>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}

suspend fun HttpClient.createRound(auctionId: String): Result<ApiRound> {
    val createAuctionText = post("/round/create") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
            Json.encodeToString(
                CreateRound.serializer(),
                CreateRound(auctionId)
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiRound>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}

suspend fun HttpClient.changeRoundState(changeRoundState: ChangeRoundState): Result<ApiRound> {
    val createAuctionText = patch("/round/change-state") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
            Json.encodeToString(
                ChangeRoundState.serializer(),
                changeRoundState
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiRound>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}

suspend fun HttpClient.exportRoundResults(exportRound: ExportBidRound): Result<ApiBidRoundResults> {
    val createAuctionText = patch("/round/export-results") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
            Json.encodeToString(
                ExportBidRound.serializer(),
                exportRound
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiBidRoundResults>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}
suspend fun HttpClient.evaluateRound(evaluateRound: EvaluateBidRound): Result<ApiBidRoundEvaluation> {
    val createAuctionText = patch("/round/evaluate") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
            Json.encodeToString(
                EvaluateBidRound.serializer(),
                evaluateRound
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiBidRoundEvaluation>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}


suspend fun HttpClient.importBidders(importBidders: ImportBidders): Result<ApiAuction> {
    val createAuctionText = post("/auction/bidder/import") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
            Json.encodeToString(
                ImportBidders.serializer(),
                importBidders
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiAuction>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}



suspend fun HttpClient.sendBid(bid: ApiBid): Result<ApiBidRound> {
    val response = post("/bid/send") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
            Json.encodeToString(
                Bid.serializer(),
                bid
            )
        )
    }
    val result = Json.decodeFromString<Result<ApiBidRound>>(ResultSerializer(),response.bodyAsText())
    return result
}
