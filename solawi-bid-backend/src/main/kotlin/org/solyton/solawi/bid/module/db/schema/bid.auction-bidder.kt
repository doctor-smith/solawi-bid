package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.id.UUIDTable

typealias AuctionBiddersTable = AuctionBidders

object AuctionBidders : UUIDTable("auction_bidders") {
    val auctionId = reference("auction_id", Auctions)
    val bidderId = reference("bidder_id", Bidders)

    init{
        index(true, auctionId, bidderId)
    }
}
