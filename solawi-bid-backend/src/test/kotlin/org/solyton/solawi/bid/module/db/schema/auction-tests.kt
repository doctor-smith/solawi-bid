package org.solyton.solawi.bid.module.db.schema

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.insert
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Schema
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.permission.schema.Context
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import kotlin.test.assertEquals

class AuctionTests {

    @Schema
    @Test
    fun createAuction() = runSimpleH2Test(
        Auctions,
        Bidders,
        Rounds,
        AcceptedRoundsTable,
        AuctionBidders,
        ContextsTable,
    ) {

        val context = Context.new {
            this.name = "context"
            createdBy = UUID_ZERO
        }

        val auctionType = AuctionType.new {
            type = "TYPE"
        }

        val name = "TestAuction"
        val auction = Auction.new {
            this.name = name
            date = DateTime.now()
            createdBy = UUID_ZERO
            type = auctionType
            this.context = context
        }

        val readAuction = Auction.find {
            Auctions.name eq name
        }.first()

        assertEquals(name, auction.name)
        assertEquals(auction, readAuction)

        println("auction.rounds = "+auction.rounds.count())
    }

    @Schema@Test
    fun addBiddersToAuction() = runSimpleH2Test(
        AuctionBidders,
        Auctions,
        Bidders,
        ContextsTable,
    ) {
        val auctionType = AuctionType.new {
            type = "TYPE"
        }

        val name = "TestAuction"
        val context = Context.new {
            this.name = "context"
            createdBy = UUID_ZERO
        }
        val auction = Auction.new {
            this.name = name
            date = DateTime.now()
            createdBy = UUID_ZERO
            type = auctionType
            this.context = context
        }

        assertEquals(name, auction.name)

        val bidder = Bidder.new {
            username = "name"
            weblingId = 1
            numberOfShares = 1
            createdBy = UUID_ZERO
            type = auctionType
        }

        AuctionBidders.insert {
            it[auctionId] = auction.id.value
            it[bidderId] = bidder.id.value
        }

        auction.bidders+bidder
        assertEquals(1,auction.bidders.count())
    }
}
