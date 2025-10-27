package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.id.UUIDTable

typealias OrganizationAuctionsTable = OrganizationAuctions

object OrganizationAuctions : UUIDTable("organization_auctions") {
    val auctionId = reference("auction_id", AuctionsTable).uniqueIndex()
    val organizationId = uuid("organization_id")
}
