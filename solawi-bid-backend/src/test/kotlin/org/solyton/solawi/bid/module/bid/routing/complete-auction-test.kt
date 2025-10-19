package org.solyton.solawi.bid.module.bid.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.kotlinx.date.todayWithTime
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.map
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.testFramework.getTestToken
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

            // get token
            val token = client.getTestToken("user@solyton.org")

            val auctionResult = client.createAuction("test-auction", token)
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
                ),
                token
            )
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
            val auctionWithBiddersResult = client.importBidders(importBidders, token )
            assertIs<Result.Success<ApiAuction>>(auctionWithBiddersResult)

            // create round
            val round1Result = client.createRound(auctionId,token )
            assertIs<Result.Success<ApiRound>>(round1Result)

            val round1 = round1Result.data
            val link1 = round1.link

            // start round
            val startedRound1 = client.changeRoundState(ChangeRoundState(round1.id, RoundState.Started.toString()), token)
            assertIs<Result.Success<ApiRound>>(startedRound1)

            // bid
            val bid1Result = client.sendBid(Bid("bidder_1@auction.com", link1, 100.0))
            assertIs<Result.Success<ApiBidRound>>(bid1Result)

            val bid2Result = client.sendBid(Bid("bidder_2@auction.com", link1, 200.0))
            assertIs<Result.Success<ApiBidRound>>(bid2Result)


            // stop round
            val stoppedRound1 = client.changeRoundState(ChangeRoundState(round1.id, RoundState.Stopped.toString()), token)
            assertIs<Result.Success<ApiAuction>>(stoppedRound1)

            // check results
            val results1Result = client.exportRoundResults(ExportBidRound(round1.id, auctionId), token)
            assertIs<Result.Success<ApiBidRoundResults>>(results1Result)
            //val results1 = results1Result.data

            val expected1: List<BidResult> = listOf(
                BidResult("bidder_1@auction.com",1,100.0,true,1 ),
                BidResult("bidder_2@auction.com",1,200.0,true,1 ),
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

            val evaluation1Result = client.evaluateRound(EvaluateBidRound(auctionId,round1.id), token)
            assertIs<Result.Success<ApiBidRoundEvaluation>>(evaluation1Result)
            val evaluation1 = evaluation1Result.data
            assertEquals(expectedEvaluation1, evaluation1)

            // evaluated round
            val evaluatedRound1 = client.changeRoundState(ChangeRoundState(round1.id, RoundState.Evaluated.toString()), token)
            assertIs<Result.Success<ApiAuction>>(evaluatedRound1)

            // close round
            val closedRound1 = client.changeRoundState(ChangeRoundState(round1.id, RoundState.Closed.toString()), token)
            assertIs<Result.Success<ApiAuction>>(closedRound1)

            // freeze round
            val frozenRound1 = client.changeRoundState(ChangeRoundState(round1.id, RoundState.Frozen.toString()), token)
            assertIs<Result.Success<ApiAuction>>(frozenRound1)

            // assert that no round has been accepted
            // Check auction
            val acceptedAuction1 = client.getAuctionById(auctionId, token)
            assertIs<Result.Success<ApiAuction>>(acceptedAuction1)

            assertEquals(null, acceptedAuction1.data.acceptedRoundId)



            // create round 2
            val round2Result = client.createRound(auctionId, token)
            assertIs<Result.Success<ApiRound>>(round2Result)

            val round2 = round2Result.data
            val link2 = round2.link

            // start round
            val startedRound2 = client.changeRoundState(ChangeRoundState(round2.id, RoundState.Started.toString()), token)
            assertIs<Result.Success<ApiRound>>(startedRound2)

            // bid
            val bid1Result2 = client.sendBid(Bid("bidder_1@auction.com", link2, 200.0))
            assertIs<Result.Success<ApiBidRound>>(bid1Result2)


            // check results
            val results2Result = client.exportRoundResults(ExportBidRound(round2.id, auctionId), token)
            assertIs<Result.Success<ApiBidRoundResults>>(results2Result)
            //val results1 = results1Result.data

            val expected2: List<BidResult> = listOf(
                BidResult("bidder_1@auction.com",1,200.0,true,2 ),
                BidResult("bidder_2@auction.com",1,200.0,true,1 ),
                BidResult("bidder_3@auction.com",1,100.0,false ),
                BidResult("bidder_4@auction.com",1,100.0,false ),
                BidResult("bidder_5@auction.com",1,100.0,false ),
                BidResult("bidder_6@auction.com",2,100.0,false )
            )
            val bidResults2 = results2Result.data.results
            assertEquals(expected2, bidResults2)

            // stop round
            val stoppedRound2 = client.changeRoundState(ChangeRoundState(round2.id, RoundState.Stopped.toString()), token)
            assertIs<Result.Success<ApiAuction>>(stoppedRound2)

            // check evaluation
            val expectedEvaluation2 = ApiBidRoundEvaluation(
                auctionDetails,
                12.0 * 900.0,
                7,
                listOf(
                    WeightedBid(1,200.0),
                    WeightedBid(1,200.0),
                    WeightedBid(1,100.0),
                    WeightedBid(1,100.0),
                    WeightedBid(1,100.0),
                    WeightedBid(2,100.0),
                )
            )

            val evaluation2Result = client.evaluateRound(EvaluateBidRound(auctionId,round2.id), token)
            assertIs<Result.Success<ApiBidRoundEvaluation>>(evaluation2Result)
            val evaluation2 = evaluation2Result.data
            assertEquals(expectedEvaluation2, evaluation2)

            // evaluated round
            val evaluatedRound2 = client.changeRoundState(ChangeRoundState(round2.id, RoundState.Evaluated.toString()), token)
            assertIs<Result.Success<ApiAuction>>(evaluatedRound2)


            // close round
            val closedRound2 = client.changeRoundState(ChangeRoundState(round2.id, RoundState.Closed.toString()), token)
            assertIs<Result.Success<ApiAuction>>(closedRound2)

            // freeze round
            val frozenRound2 = client.changeRoundState(ChangeRoundState(round2.id, RoundState.Frozen.toString()), token)
            assertIs<Result.Success<ApiAuction>>(frozenRound2)

            // Accept round
            val acceptedRound = client.acceptRound(ApiAcceptRound(auction.id, round2.id), token)
            assertIs<Result.Success<ApiAcceptedRound>>(acceptedRound)

            // Check auction
            val acceptedAuction2 = client.getAuctionById(auctionId, token)
            assertIs<Result.Success<ApiAuction>>(acceptedAuction2)

            assertEquals(round2.id, acceptedAuction2.data.acceptedRoundId)


        }
    }
}

suspend fun HttpClient.createAuction(name: String, token: String): Result<ApiAuction> {
    val createAuctionText = post("/auction/create") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
        setBody(
            Json.encodeToString(
                CreateAuction.serializer(),
                CreateAuction(name, todayWithTime())
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiAuction>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}

suspend fun HttpClient.configureAuction(configureAuction: ConfigureAuction, token: String): Result<ApiAuction> {
    val createAuctionText = patch("/auction/configure") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
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

suspend fun HttpClient.getAuctionById(auctionId: String, token: String): Result<ApiAuction> {
    val createAuctionText = get("/auction/all") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiAuctions>>(
        ResultSerializer(),
        createAuctionText
    ).map { auctions -> auctions.list.first { auction -> auction.id == auctionId } }
    return result
}

suspend fun HttpClient.createRound(auctionId: String, token: String): Result<ApiRound> {
    val createAuctionText = post("/round/create") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
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

suspend fun HttpClient.changeRoundState(changeRoundState: ChangeRoundState, token: String): Result<ApiRound> {
    val createAuctionText = patch("/round/change-state") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
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

suspend fun HttpClient.exportRoundResults(exportRound: ExportBidRound, token: String): Result<ApiBidRoundResults> {
    val createAuctionText = patch("/round/export-results") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
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
suspend fun HttpClient.evaluateRound(evaluateRound: EvaluateBidRound, token: String): Result<ApiBidRoundEvaluation> {
    val createAuctionText = patch("/round/evaluate") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
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

suspend fun HttpClient.acceptRound(acceptRound: ApiAcceptRound, token: String): Result<ApiAcceptedRound> {
    val createAuctionText = patch("/auction/accept-round") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
        setBody(
            Json.encodeToString(
                ApiAcceptRound.serializer(),
                acceptRound
            )
        )
    }.bodyAsText()
    val result = Json.decodeFromString<Result<ApiAcceptedRound>>(
        ResultSerializer(),
        createAuctionText
    )
    return result
}

suspend fun HttpClient.importBidders(importBidders: ImportBidders, token: String): Result<ApiAuction> {
    val createAuctionText = post("/auction/bidder/import") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
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
