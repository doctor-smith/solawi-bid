package org.solyton.solawi.bid.module.bid.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.routing.findUserId
import org.solyton.solawi.bid.module.bid.action.db.addRound
import org.solyton.solawi.bid.module.bid.action.db.createAuction
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.values.AuctionId
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.bid.service.assignAuctionRoles
import org.solyton.solawi.bid.module.permission.repository.cloneRightRoleContext
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.repository.createRightIfNotExists
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.values.ProviderId
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals

class AuctionAuthorizationRoutingTests {
    @Api @Test
    fun existingAuctionResourcesAreBoundToTheirStoredContext() = runBlocking {
        val url = "jdbc:h2:mem:auction_authz_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseString("database.url = \"$url\"")
                        .withFallback(ConfigFactory.parseFile(File("src/test/resources/bid.api.test.conf"))).resolve()
                )
            }
            val token = client.getTestToken(manager)
            val database = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")
            val (own, foreign) = transaction(database) {
                val seed = ContextEntity.find { ContextsTable.name eq "AUCTIONS" }.single()
                createRightIfNotExists("READ_ORGANIZATION", "Fixture right", UUID_ZERO)
                val own = seedAuction(seed, "Owned fixture")
                val foreign = seedAuction(seed, "Separate fixture")
                assignAuctionRoles(findUserId(manager), UUID.fromString(own.context), "AUCTION_MANAGER")
                own to foreign
            }
            fun state() = transaction(database) {
                val tables = listOf(AuctionsTable, AuctionBiddersTable, RoundCommentsTable, AcceptedRoundsTable)
                val rows = tables.associateWith { table ->
                    table.selectAll().map { row -> table.columns.map { row[it] } }.toSet()
                }
                val rounds = RoundsTable.select(RoundsTable.id, RoundsTable.state, RoundsTable.auction).map {
                    Triple(it[RoundsTable.id], it[RoundsTable.state], it[RoundsTable.auction])
                }.toSet()
                rows to rounds
            }
            val before = state()
            val operations = listOf(
                operation(HttpMethod.Patch, "auction/configure", ConfigureAuction(own.auction, "Changed", foreign.context, date)),
                operation(HttpMethod.Delete, "auction/delete", DeleteAuctions(listOf(own.auction, foreign.auction))),
                operation(HttpMethod.Patch, "auction/accept-round", AcceptRound(foreign.auction, foreign.round)),
                operation(
                    HttpMethod.Post, "auction/bidder/import",
                    ImportBidders(foreign.auction, listOf(NewBidder("bidder@example.test", 0, 1)))
                ),
                operation(
                    HttpMethod.Post, "auction/bidder/import-bidders-from-organization",
                    ImportBiddersFromOrganization(ProviderId(foreign.organization), AuctionId(own.auction))
                ),
                operation(HttpMethod.Post, "round/create", CreateRound(foreign.auction)),
                operation(HttpMethod.Patch, "round/change-state", ChangeRoundState(foreign.round, RoundState.Started.toString())),
                operation(HttpMethod.Patch, "round/export-results", ExportBidRound(foreign.round, foreign.auction)),
                operation(HttpMethod.Patch, "round/evaluate", EvaluateBidRound(auctionId = foreign.auction, roundId = foreign.round)),
                operation(HttpMethod.Patch, "round/pre-evaluate", PreEvaluateBidRound(auctionId = foreign.auction, roundId = foreign.round)),
                operation(HttpMethod.Post, "round/add-comment", CommentOnRound("Fixture note", foreign.round))
            )
            for (operation in operations) {
                assertEquals(HttpStatusCode.Forbidden, client.runOperation(operation, token, own.context), operation.path)
                assertEquals(before, state(), operation.path)
            }

            val unrelatedRound = operation(HttpMethod.Patch, "auction/accept-round", AcceptRound(own.auction, foreign.round))
            assertEquals(HttpStatusCode.NotFound, client.runOperation(unrelatedRound, token, own.context))
            assertEquals(before, state())

            val ownComment = operation(HttpMethod.Post, "round/add-comment", CommentOnRound("Fixture note", own.round))
            assertEquals(HttpStatusCode.OK, client.runOperation(ownComment, token, foreign.context))
            transaction(database) {
                val comment = RoundCommentsTable.selectAll().single()
                assertEquals(own.round, comment[RoundCommentsTable.roundId].value.toString())
            }
        }
    }

    private fun Transaction.seedAuction(seed: ContextEntity, name: String): AuctionFixture {
        val context = createRootContext(name)
        cloneRightRoleContext(seed.id.value, context.id.value)
        val organization = OrganizationEntity.new {
            this.name = name
            this.context = context
            createdBy = UUID_ZERO
        }
        val auction = createAuction(name, date, contextId = context.id.value).id.value.toString()
        val round = addRound(CreateRound(auction)).id.value.toString()
        return AuctionFixture(context.id.value.toString(), organization.id.value.toString(), auction, round)
    }

    private suspend fun HttpClient.runOperation(operation: Operation, token: String, context: String) =
        request(operation.path) {
            method = operation.method
            bearerAuth(token)
            header(Header.CONTEXT, context)
            contentType(ContentType.Application.Json)
            setBody(operation.body)
        }.status

    private inline fun <reified T> operation(method: HttpMethod, path: String, input: T) =
        Operation(method, path, Json.encodeToString(input))

    private data class Operation(val method: HttpMethod, val path: String, val body: String)

    private data class AuctionFixture(val context: String, val organization: String, val auction: String, val round: String)

    private companion object {
        const val manager = "auction.manager@solyton.org"
        val date = LocalDateTime(2026, 11, 27, 9, 0)
    }
}
