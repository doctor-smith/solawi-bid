package org.solyton.solawi.bid.module.db.schema

import org.evoleq.exposedx.test.runSimpleH2Test
import org.jetbrains.exposed.sql.insert
import org.solyton.solawi.bid.module.bid.schema.AcceptedRoundsTable
import org.solyton.solawi.bid.module.bid.schema.Auction
import org.solyton.solawi.bid.module.bid.schema.AuctionBidders
import org.solyton.solawi.bid.module.bid.schema.Auctions
import org.solyton.solawi.bid.module.bid.schema.Bidder
import org.solyton.solawi.bid.module.bid.schema.Bidders
import org.solyton.solawi.bid.module.bid.schema.Rounds
import kotlin.test.assertEquals

class AuctionTests {

    // @Schema@Test
    fun createAuction() = runSimpleH2Test(
        AuctionBidders,
        Auctions,
        Bidders,
        Rounds,
        AcceptedRoundsTable
    ) {
        val name = "TestAuction"
        val auction = Auction.new {
            this.name = name
        }

        val readAuction = Auction.find {
            Auctions.name eq name
        }.first()

        assertEquals(name, auction.name)
        assertEquals(auction, readAuction)

        println("auction.rounds = "+auction.rounds.count())
    }

    //@Schema@Test
    fun addBiddersToAuction() = runSimpleH2Test(
        AuctionBidders,
        Auctions,
        Bidders,
    ) {
        val name = "TestAuction"
        val auction = Auction.new {
            this.name = name
        }

        assertEquals(name, auction.name)

        val bidder = Bidder.new {
            username = "name"
            weblingId = 1
            numberOfShares = 1
        }

        AuctionBidders.insert {
            it[auctionId] = auction.id.value
            it[bidderId] = bidder.id.value
        }

        //auction.bidders+bidder

        println(bidder.auctions.map { it.name })

        println()
    }
}
