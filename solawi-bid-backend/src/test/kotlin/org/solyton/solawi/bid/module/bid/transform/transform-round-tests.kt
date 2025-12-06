package org.solyton.solawi.bid.module.bid.transform

import kotlinx.datetime.LocalDate
import org.evoleq.exposedx.joda.toKotlinxWithZone
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.kotlinx.date.todayWithTime
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.bid.action.db.addRound
import org.solyton.solawi.bid.module.bid.action.db.createAuction
import org.solyton.solawi.bid.module.bid.data.api.ApiRound
import org.solyton.solawi.bid.module.bid.data.api.ApiRoundComment
import org.solyton.solawi.bid.module.bid.data.api.ApiRoundComments
import org.solyton.solawi.bid.module.bid.data.api.CreateRound
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.repository.addComment
import org.solyton.solawi.bid.module.bid.schema.AcceptedRoundsTable
import org.solyton.solawi.bid.module.bid.schema.AuctionTypesTable
import org.solyton.solawi.bid.module.bid.schema.AuctionsTable
import org.solyton.solawi.bid.module.bid.schema.OrganizationAuctionsTable
import org.solyton.solawi.bid.module.bid.schema.RoundCommentsTable
import org.solyton.solawi.bid.module.bid.schema.RoundsTable
import org.solyton.solawi.bid.module.permission.schema.Context
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import kotlin.test.assertEquals

class TransformRoundTests {
    @DbFunctional@Test fun transformRoundComment() =
        runSimpleH2Test(
            AuctionsTable,
            AuctionTypesTable,
            AcceptedRoundsTable,
            RoundsTable,
            RoundCommentsTable,
            ContextsTable,OrganizationsTable,
            OrganizationAuctionsTable
        ) {
            val context = Context.new {
                this.name = "context"
                createdBy = UUID_ZERO
            }

            // val organization =
            OrganizationEntity.new {
                name = "organization"
                createdBy = UUID_ZERO
                this.context = context
            }

            AuctionTypesTable.insert {
                it[type] = "AUCTION_TYPE"
            }

            val auction = createAuction("name", todayWithTime(), "AUCTION_TYPE", context.id.value)
            val round = addRound(CreateRound("${auction.id.value}"))
            round.addComment("comment-1", UUID_ZERO)


            val roundComment = round.comments.first()
            val expected = ApiRoundComment(
                roundComment.id.value.toString(),
                comment = roundComment.comment,
                createAt = roundComment.createdAt.toKotlinxWithZone(),
                createdBy = roundComment.createdBy.toString()
            )

            val apiComment = roundComment.toApiType()
            assertEquals(
                expected,
                apiComment
            )
        }
    @DbFunctional@Test fun transformRoundComments() =
        runSimpleH2Test(
            AuctionsTable,
            AuctionTypesTable,
            AcceptedRoundsTable,
            RoundsTable,
            RoundCommentsTable,
            ContextsTable,OrganizationsTable,
            OrganizationAuctionsTable
        ) {
            val context = Context.new {
                this.name = "context"
                createdBy = UUID_ZERO
            }

            // val organization =
            OrganizationEntity.new {
                name = "organization"
                createdBy = UUID_ZERO
                this.context = context
            }

            AuctionTypesTable.insert {
                it[type] = "AUCTION_TYPE"
            }

            val auction = createAuction("name", todayWithTime(), "AUCTION_TYPE", context.id.value)
            val round = addRound(CreateRound("${auction.id.value}"))
            round.addComment("comment-1", UUID_ZERO)


            val roundComment = round.comments.first()
            val expected = ApiRoundComments(
                listOf(
                    ApiRoundComment(
                        roundComment.id.value.toString(),
                        comment = roundComment.comment,
                        createAt = roundComment.createdAt.toKotlinxWithZone(),
                        createdBy = roundComment.createdBy.toString()
                    )
                )
            )

            val apiComments = ApiRoundComments(listOf(roundComment.toApiType()))
            assertEquals(
                expected,
                apiComments
            )
        }

    @DbFunctional@Test fun transformRound() =
        runSimpleH2Test(
            AuctionsTable,
            AuctionTypesTable,
            AcceptedRoundsTable,
            RoundsTable,
            RoundCommentsTable,
            ContextsTable,OrganizationsTable,
            OrganizationAuctionsTable
        ) {
            val context = Context.new {
                this.name = "context"
                createdBy = UUID_ZERO
            }

            // val organization =
            OrganizationEntity.new {
                name = "organization"
                createdBy = UUID_ZERO
                this.context = context
            }
            AuctionTypesTable.insert {
                it[type] = "AUCTION_TYPE"
            }

            val auction = createAuction("name", todayWithTime(), "AUCTION_TYPE", context.id.value)
            val round = addRound(CreateRound("${auction.id.value}"))
            round.addComment("comment-1", UUID_ZERO)


            val roundComment = round.comments.first()
            val expectedApiRoundComments = ApiRoundComments(
                listOf(
                    ApiRoundComment(
                        roundComment.id.value.toString(),
                        comment = roundComment.comment,
                        createAt = roundComment.createdAt.toKotlinxWithZone(),
                        createdBy = roundComment.createdBy.toString()
                    )
                )
            )
            val expected = ApiRound(
                id = round.id.value.toString(),
                link = round.link,
                state = round.state,
                number = round.number,
                comments = expectedApiRoundComments
            )
            val apiRound = round.toApiType()
            assertEquals(
                expected,
                apiRound
            )
        }
}
