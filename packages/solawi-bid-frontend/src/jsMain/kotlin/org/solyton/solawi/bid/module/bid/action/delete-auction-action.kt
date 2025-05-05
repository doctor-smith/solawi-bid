package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.ApiAuctions
import org.solyton.solawi.bid.module.bid.data.api.DeleteAuctions
import org.solyton.solawi.bid.module.bid.data.toDomainType

val deleteAuctionAction: (Auction)->Action<Application, DeleteAuctions, ApiAuctions> by lazy {
    { auction: Auction -> Action<Application, DeleteAuctions, ApiAuctions>(
        name = "DeleteAuction",
        reader = { DeleteAuctions(listOf(auction.auctionId)) },
        endPoint = DeleteAuctions::class,
        writer = auctions.set contraMap { apiAuctions: ApiAuctions -> apiAuctions.toDomainType() }
    ) }
}
