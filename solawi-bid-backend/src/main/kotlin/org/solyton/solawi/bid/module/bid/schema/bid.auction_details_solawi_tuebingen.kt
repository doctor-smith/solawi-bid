package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias AuctionDetailsSolawiTuebingenEntity = AuctionDetailsSolawiTuebingen

object AuctionDetailsSolawiTuebingenTable : AuditableUUIDTable("auction_details_solawi_tuebingen") {
    val auctionId = reference("auction_id", AuctionsTable)
    val benchmark = double("benchmark")
    val targetAmount = double("target_amount")
    val solidarityContribution = double("solidarity_contribution")
    val minimalBid = double("minimal_bid").default(0.0)
}

class AuctionDetailsSolawiTuebingen(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<AuctionDetailsSolawiTuebingen>(AuctionDetailsSolawiTuebingenTable)

    var benchmark by AuctionDetailsSolawiTuebingenTable.benchmark
    var targetAmount by AuctionDetailsSolawiTuebingenTable.targetAmount
    var solidarityContribution by AuctionDetailsSolawiTuebingenTable.solidarityContribution
    var minimalBid by AuctionDetailsSolawiTuebingenTable.minimalBid
    var auction by Auction referencedOn AuctionDetailsSolawiTuebingenTable.auctionId

    override var createdAt: DateTime by AuctionDetailsSolawiTuebingenTable.createdAt
    override var createdBy: UUID by AuctionDetailsSolawiTuebingenTable.createdBy
    override var modifiedAt: DateTime? by AuctionDetailsSolawiTuebingenTable.modifiedAt
    override var modifiedBy: UUID? by AuctionDetailsSolawiTuebingenTable.modifiedBy
}

