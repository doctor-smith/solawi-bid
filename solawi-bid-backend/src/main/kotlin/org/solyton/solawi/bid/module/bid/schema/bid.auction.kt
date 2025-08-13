package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.jodatime.date
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias AuctionEntity = Auction
typealias AuctionsTable = Auctions

object Auctions: AuditableUUIDTable("auctions") {
    val name = varchar("name", 250)
    val date = date("date")
    val typeId = reference("type_id", AuctionTypes)
}


class Auction(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Auction>(Auctions)

    var name by Auctions.name
    var date by Auctions.date
    var type by AuctionType referencedOn Auctions.typeId
    val rounds: SizedIterable<Round> by Round referrersOn Rounds.auction

    var bidders: SizedIterable<Bidder> by Bidder via AuctionBidders
    val bidRounds: SizedIterable<BidRound> by BidRound referrersOn BidRounds.auction

    override var createdAt: DateTime by Auctions.createdAt
    override var createdBy: UUID by Auctions.createdBy
    override var modifiedAt: DateTime? by Auctions.modifiedAt
    override var modifiedBy: UUID? by Auctions.modifiedBy
}

