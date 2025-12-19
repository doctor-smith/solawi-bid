package org.solyton.solawi.bid.module.bid.action.db

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.kotlinx.date.todayWithTime
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.repository.createApplication
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextsTable
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextsTable
import org.solyton.solawi.bid.module.bid.data.api.CreateRound
import org.solyton.solawi.bid.module.bid.repository.addComment
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.bid.schema.RoundEntity
import org.solyton.solawi.bid.module.permission.schema.Context
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import kotlin.test.assertEquals

class RoundTests {
    @DbFunctional@Test fun addCommentTest() = runSimpleH2Test(
        AuctionsTable,
        AuctionTypesTable,
        AcceptedRoundsTable,
        RoundsTable,
        RoundCommentsTable,
        ContextsTable,
        OrganizationsTable,
        OrganizationAuctionsTable,
        OrganizationModuleContextsTable,
        OrganizationApplicationContextsTable,
        ApplicationsTable,
        ModulesTable
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

        createApplication("AUCTIONS", "TEST APP", UUID_ZERO, false, context.id.value)
        val auction = createAuction("name", todayWithTime(), "AUCTION_TYPE", context.id.value)
        val round = addRound(CreateRound("${auction.id.value}"))
        round.addComment("comment-1", UUID_ZERO)
        assertEquals(1, round.comments.count())

        val roundWithComment = RoundEntity.find {
            RoundsTable.id eq round.id
        }.first()
        assertEquals(1, roundWithComment.comments.count())
    }
}
