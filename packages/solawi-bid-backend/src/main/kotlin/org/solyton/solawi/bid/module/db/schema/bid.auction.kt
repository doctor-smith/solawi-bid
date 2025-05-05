package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.jodatime.date
import java.util.*

typealias AuctionEntity = Auction
typealias AuctionsTable = Auctions

object Auctions: UUIDTable("auctions") {
    val name = varchar("name", 250)
    val date = date("date")
    val typeId = reference("type_id", AuctionTypes)
}


class Auction(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Auction>(Auctions)

    var name by Auctions.name
    var date by Auctions.date
    var type by AuctionType referencedOn Auctions.typeId
    val rounds: SizedIterable<Round> by Round referrersOn Rounds.auction
    /*
    val bidders: List<Bidder>
        get() = with(bidders0){
            val details = BidderDetailsSolawiTuebingenEntity.find{BidderDetailsSolawiTuebingenTable.bidderId inList bidders0.map { it.id }}
            bidders0.map{
                it.numberOfShares = details.find { det -> det.id == it.id }?.numberOfShares?:0
                it
            }

        }
    */

    var bidders: SizedIterable<Bidder> by Bidder via AuctionBidders
    val bidRounds: SizedIterable<BidRound> by BidRound referrersOn BidRounds.auction
}

