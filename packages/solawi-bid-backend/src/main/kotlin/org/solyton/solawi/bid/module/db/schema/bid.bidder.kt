package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias BidderEntity = Bidder
typealias BiddersTable = Bidders

object Bidders : UUIDTable("bidders") {

    val username = varchar("username", 100)

    val typeId = reference("type_id", AuctionTypes)
    // external Id in the webling interface
    val weblingId = integer("webling_id").default(0)

    // number of parts the prosumer wants to buy
    val numberOfShares = integer("number_of_shares").default(0)

    init{
        index(true, username, weblingId)
    }
}

class Bidder(id : EntityID<UUID> ) : UUIDEntity(id) {
    companion object: UUIDEntityClass<Bidder>(Bidders)

    var username by Bidders.username
    var weblingId by Bidders.weblingId
    var numberOfShares by Bidders.numberOfShares

    var type by AuctionType referencedOn Bidders.typeId

    var auctions by Auction via AuctionBidders
    val bidRounds by BidRound referrersOn BidRounds.bidder
}