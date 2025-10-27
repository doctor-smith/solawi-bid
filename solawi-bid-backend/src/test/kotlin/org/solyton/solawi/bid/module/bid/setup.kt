package org.solyton.solawi.bid.module.bid

import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.bid.action.db.BidBests.BidProcessSetup
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.permission.schema.Context

fun Transaction.setupBidProcess(): BidProcessSetup {
    val context = Context.new {
        this.name = "context"
        createdBy = UUID_ZERO
    }

    // db setup
    val auctionType = AuctionType.new {
        type = "SOLAWI_TUEBINGEN"
    }

    // create an auction
    val auction = Auction.new {
        name = "TestAuction"
        date = DateTime().withDate(1,1,1)
        type = auctionType
        createdBy = UUID_ZERO
        this.context = context
    }
    // create a round in the auction
    // note: state is "OPENED" by default.
    val round = Round.new {
        this.auction = auction
        link = "test-link"
        number = 1
        createdBy = UUID_ZERO
        // state = RoundState.Started.toString()
    }
    auction.rounds+round
    // invite bidders to the auction
    val bidder = Bidder.new {
        username = "test-user"
        type = auctionType
        createdBy = UUID_ZERO
    }
    AuctionBidders.insert{
        it[AuctionBidders.auctionId] = auction.id
        it[AuctionBidders.bidderId] = bidder.id
    }
    BidderDetailsSolawiTuebingenTable.insert {
        it[weblingId] = 1
        it[numberOfShares] = 1
        it[bidderId] = bidder.id.value
        it[createdBy] = UUID_ZERO
    }
    auction.bidders+bidder

    return BidProcessSetup(auction, round, bidder)
}
