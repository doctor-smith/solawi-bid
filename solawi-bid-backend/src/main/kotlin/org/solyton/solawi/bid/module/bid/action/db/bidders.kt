package org.solyton.solawi.bid.module.bid.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.solyton.solawi.bid.module.bid.data.api.ImportBidders
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.db.BidRoundException
import org.solyton.solawi.bid.module.db.schema.AuctionBidders
import org.solyton.solawi.bid.module.db.schema.AuctionEntity
import org.solyton.solawi.bid.module.db.schema.Auctions
import org.solyton.solawi.bid.module.db.schema.BiddersTAble
import java.util.UUID

@MathDsl
val ImportBidders = KlAction{bidders: Result<ImportBidders> -> DbAction {
    database: Database -> bidders bindSuspend  {
        resultTransaction(database) {
            importBidders(auctionId = UUID.fromString(it.auctionId), it.bidders).toApiType()
        }
    } x database
}}

fun Transaction.importBidders(auctionId: UUID, bidders: List<NewBidder>): AuctionEntity {
    val auction = AuctionEntity.find { Auctions.id eq auctionId }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction
    val biddersToDelete = auction.bidders.map { it.id }
    AuctionBidders.deleteWhere { AuctionBidders.bidderId inList biddersToDelete }
    BiddersTAble.deleteWhere { BiddersTAble.id inList biddersToDelete }

    return addBidders(auction,bidders)
}