package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias BidderEntity = Bidder
typealias BiddersTable = Bidders

object Bidders : AuditableUUIDTable("bidders") {

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

class Bidder(id : EntityID<UUID> ) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object: UUIDEntityClass<Bidder>(Bidders)

    var username by Bidders.username
    var weblingId by Bidders.weblingId
    var numberOfShares by Bidders.numberOfShares

    var type by AuctionType referencedOn Bidders.typeId

    var auctions by Auction via AuctionBidders
    val bidRounds by BidRound referrersOn BidRounds.bidder

    override var createdAt: DateTime by Bidders.createdAt
    override var createdBy: UUID by Bidders.createdBy
    override var modifiedAt: DateTime? by Bidders.modifiedAt
    override var modifiedBy: UUID? by Bidders.modifiedBy
}
