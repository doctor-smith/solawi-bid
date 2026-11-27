package org.solyton.solawi.bid.module.bid.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.bid.schema.BidRoundEntity
import org.solyton.solawi.bid.module.bid.schema.RoundEntity
import org.solyton.solawi.bid.module.bid.schema.RoundsTable
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals

class BidRoutingTests {
    @Api @Test
    fun publicBidWritesOnlyWhenItsRoundIsStarted() = runBlocking {
        val url = "jdbc:h2:mem:public_bid_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseString("database.url = \"$url\"")
                        .withFallback(ConfigFactory.parseFile(File("src/test/resources/bid.api.test.conf"))).resolve()
                )
            }
            startApplication()
            val database = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")
            val bid = Bid("test-user", amount = 1.0, link = "test-link")
            suspend fun send() = client.post("/bid/send") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(Bid.serializer(), bid))
            }
            fun storedAmounts() = transaction(database) { BidRoundEntity.all().map { it.amount } }

            val before = storedAmounts()
            val closed = send()
            assertEquals(HttpStatusCode.Conflict, closed.status)
            val error = Json.decodeFromString(Result.Failure.Message.serializer(), closed.bodyAsText())
            assertEquals(BidRoundException.RoundNotStarted.message, error.value)
            assertEquals(before, storedAmounts())

            transaction(database) {
                RoundEntity.find { RoundsTable.link eq bid.link }.single().state = RoundState.Started.toString()
            }
            assertEquals(HttpStatusCode.OK, send().status)
            assertEquals(before + bid.amount, storedAmounts())
        }
    }
}
