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
import org.solyton.solawi.bid.module.bid.data.api.Auction
import org.solyton.solawi.bid.module.bid.data.api.CreateAuction
import org.solyton.solawi.bid.module.bid.data.api.ImportBidders
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import java.io.File
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BidderTests {

    @Api@Test
    fun importBidders() = runBlocking {
        testApplication {
            environment {
                // Load the HOCON file explicitly with the file path
                val configFile = File("src/test/resources/bid.api.test.conf")
                config = HoconApplicationConfig(ConfigFactory.parseFile(configFile))

            }
            application { }
            // create Auction
            val auctionText = client.post("/auction/create") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateAuction.serializer(),
                        CreateAuction("test-name-2", LocalDate(1,1,1))
                    )
                )
            }.bodyAsText()
            val auctionResult = Json.decodeFromString(ResultSerializer, auctionText)
            assertIs<Result.Success<Auction>>(auctionResult)
            val auction = auctionResult.data

            val bidder = NewBidder(
                username,
                0,
                2
            )
            val importBiddersResponse = client.post("/auction/bidder/import") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        ImportBidders.serializer(),
                        ImportBidders(auction.id, listOf(bidder))
                    )
                )
            }
            val importBiddersResult = Json.decodeFromString(
                ResultSerializer,
                importBiddersResponse.bodyAsText()
            )
            assertIs<Result.Success<Auction>>(importBiddersResult)
            val nextAuction = importBiddersResult.data
            assertTrue{ nextAuction.bidderIds.isNotEmpty() }
        }
    }
}

const val username = "developer@alpha-structure.com"
const val password = "pass1234"