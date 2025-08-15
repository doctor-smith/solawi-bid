package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias BidderDetailsSolawiTuebingenEntity = BidderDetailsSolawiTuebingen
typealias BidderDetailsEntity = BidderDetails

object BidderDetailsSolawiTuebingenTable : AuditableUUIDTable("bidder_details_solawi_tuebingen") {

    val bidderId = reference("bidder_id", Bidders)
    // external Id in the webling interface
    val weblingId = integer("webling_id")

    // number of parts the prosumer wants to buy
    val numberOfShares = integer("number_of_shares")
}

class BidderDetailsSolawiTuebingen (id: EntityID<UUID>) : UUIDEntity(id), BidderDetails.SolawiTuebingen, AuditableEntity<UUID> {
    companion object : UUIDEntityClass<BidderDetailsSolawiTuebingen>(BidderDetailsSolawiTuebingenTable)

    //var bidderId by BidderDetailsSolawiTuebingenTable.bidderId
    var weblingId by BidderDetailsSolawiTuebingenTable.weblingId
    override var numberOfShares by BidderDetailsSolawiTuebingenTable.numberOfShares

    override var bidder by Bidder referencedOn BidderDetailsSolawiTuebingenTable.bidderId

    override var createdAt: DateTime by BidderDetailsSolawiTuebingenTable.createdAt
    override var createdBy: UUID by BidderDetailsSolawiTuebingenTable.createdBy
    override var modifiedAt: DateTime? by BidderDetailsSolawiTuebingenTable.modifiedAt
    override var modifiedBy: UUID? by BidderDetailsSolawiTuebingenTable.modifiedBy
}

sealed interface BidderDetails {
    interface SolawiTuebingen : BidderDetails {
        var bidder: Bidder
        var numberOfShares: Int
    }
}
