package org.solyton.solawi.bid.module.bid.action.db

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.kotlinx.date.todayWithTime
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.selectAll
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.repository.createApplication
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextsTable
import org.solyton.solawi.bid.module.application.schema.OrganizationModuleContextsTable
import org.solyton.solawi.bid.module.bid.data.api.CreateRound
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.bid.schema.AuctionEntity
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.permission.schema.Context
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class AuctionTests {

    @DbFunctional@Test fun createAuction() = runSimpleH2Test(
        AuctionBidders,
        AcceptedRoundsTable,
        Auctions,
        Bidders,
        Rounds
    ) {

    }

    @DbFunctional@Test fun prepareAuction() = runSimpleH2Test(
        AuctionBidders,
        AcceptedRoundsTable,
        AuctionDetailsSolawiTuebingenTable,
        Auctions,
        Bidders,
        BidderDetailsSolawiTuebingenTable,
        Rounds,
        RoundComments,
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

        val name = "TestAuction"
        val link = "TestLink"
        AuctionType.new {
            type = "SOLAWI_TUEBINGEN"
        }

        createApplication("AUCTIONS", "TEST APP", UUID_ZERO, false, context.id.value)
        val auction = createAuction(name,todayWithTime(), contextId = context.id.value).toApiType()
        assertEquals(name, auction.name)
        val round = addRound(
            CreateRound(
                auction.id
            )
        ).toApiType()
        assertNotEquals(link, round.link)

        val bidders = listOf<NewBidder>(
            NewBidder("name1",1,1),
            NewBidder("name2",3,1),
            NewBidder("name3",3,1),
            NewBidder("name4",4,1)
        )

        val auctionWithBidders = addBidders(
            auctionId = UUID.fromString( auction.id),
            bidders
        ).toApiType()
        assertEquals(bidders.size, auctionWithBidders.bidderInfo.size)

    }


    @DbFunctional@Test fun addNewBiddersOfAuction() = runSimpleH2Test(
        AuctionBidders,
        AcceptedRoundsTable,
        AuctionDetailsSolawiTuebingenTable,
        Auctions,
        Bidders,
        BidderDetailsSolawiTuebingenTable,
        Rounds,
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

        val name = "TestAuction"
        AuctionType.new {
            type = "SOLAWI_TUEBINGEN"
        }

        createApplication("AUCTIONS", "TEST APP", UUID_ZERO, false, context.id.value)
        val auction = createAuction(name,todayWithTime(), contextId = context.id.value).toApiType()

        val bidders = listOf<NewBidder>(
            NewBidder("name1",1,1),
            NewBidder("name2",3,1),
            NewBidder("name3",3,1),
            NewBidder("name4",4,1)
        )

        val auctionWithBidders = addBidders(
            auctionId = UUID.fromString( auction.id),
            bidders
        ).toApiType()
        assertEquals(bidders.size, auctionWithBidders.bidderInfo.size)

        val newBidders = listOf<NewBidder>(
            NewBidder("name1",1,1),
            NewBidder("name2",3,1),
            NewBidder("name5",3,1),
            NewBidder("name6",4,1)
        )

        // val auctionWithNewBidders =
        addBidders(
            auctionId = UUID.fromString( auction.id),
            newBidders
        ).toApiType()
        assertEquals(6, AuctionBidders.selectAll().count())
        assertEquals(6, Bidders.selectAll().count())
    }

    @DbFunctional@Test fun addSameBiddersToDifferentAuctions() = runSimpleH2Test(
        AuctionBidders,
        AcceptedRoundsTable,
        AuctionDetailsSolawiTuebingenTable,
        Auctions,
        Bidders,
        BidderDetailsSolawiTuebingenTable,
        Rounds,
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

        val name = "TestAuction"
        AuctionType.new {
            type = "SOLAWI_TUEBINGEN"
        }

        createApplication("AUCTIONS", "TEST APP", UUID_ZERO, false, context.id.value)
        val auction1 = createAuction(name, todayWithTime(), contextId = context.id.value)
        assertEquals(name, auction1.name)

        val auction2 = createAuction(name, todayWithTime(), contextId = context.id.value)
        assertEquals(name, auction2.name)


        val bidders = listOf<NewBidder>(
            NewBidder("name1", 1, 1),
            NewBidder("name2", 3, 1),
            NewBidder("name3", 3, 1),
            NewBidder("name4", 4, 1)
        )

        addBidders(auction1, bidders)
        addBidders(auction2, bidders)

        assertEquals(8, AuctionBidders.selectAll().count())
    }

    @DbFunctional@Test
    fun deleteAuction() = runSimpleH2Test(
        AuctionBidders,
        AcceptedRoundsTable,
        AuctionDetailsSolawiTuebingenTable,
        Auctions,
        Bidders,
        BidderDetailsSolawiTuebingenTable,
        Rounds,
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

        val auctionType = AuctionType.new {
            type = "SOLAWI_TUEBINGEN"
        }
        val auction = AuctionEntity.new {
            name = "TestAuction"
            date = DateTime().withDate(1,1,1)
            type = auctionType
            createdBy = UUID_ZERO
            this.context = context
        }

        deleteAuctions(listOf(auction.id.value))

        val a = AuctionEntity.find { Auctions.id eq auction.id }.firstOrNull()
        assertNull(a)
    }
}
