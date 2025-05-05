package org.solyton.solawi.bid.module.bid.action.db

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.db.BidRoundException
import org.solyton.solawi.bid.module.db.schema.AcceptedRoundEntity
import org.solyton.solawi.bid.module.db.schema.AcceptedRoundsTable
import org.solyton.solawi.bid.module.db.schema.AuctionEntity
import org.solyton.solawi.bid.module.db.schema.Round
import java.util.*


fun Transaction.validateAuctionNotAccepted(auction: AuctionEntity) {
    validateAuctionNotAccepted(auction.id.value)
}

fun Transaction.validateAuctionNotAccepted(auctionId: UUID) {
    val auctionAccepted = !AcceptedRoundEntity.find { AcceptedRoundsTable.auctionId eq auctionId}.empty()
    if(auctionAccepted) throw BidRoundException.AuctionAccepted
}

fun Transaction.validateAuctionNotAccepted(round: Round) {
    validateAuctionNotAccepted(round.auction)
}
