package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias AuctionDetailsSolawiTuebingenEntity = AuctionDetailsSolawiTuebingen

object AuctionDetailsSolawiTuebingenTable : UUIDTable("auction_details_solawi_tuebingen") {
    val auctionId = reference("auction_id", AuctionsTable)
    val benchmark = double("benchmark")
    val targetAmount = double("target_amount")
    val solidarityContribution = double("solidarity_contribution")
    val minimalBid = double("minimal_bid").default(0.0)
}

class AuctionDetailsSolawiTuebingen(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AuctionDetailsSolawiTuebingen>(AuctionDetailsSolawiTuebingenTable)

    var benchmark by AuctionDetailsSolawiTuebingenTable.benchmark
    var targetAmount by AuctionDetailsSolawiTuebingenTable.targetAmount
    var solidarityContribution by AuctionDetailsSolawiTuebingenTable.solidarityContribution
    var minimalBid by AuctionDetailsSolawiTuebingenTable.minimalBid
    var auction by Auction referencedOn AuctionDetailsSolawiTuebingenTable.auctionId
}

