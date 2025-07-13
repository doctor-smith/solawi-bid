package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiAuctions
import org.solyton.solawi.bid.module.bid.data.api.DeleteAuctions
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auctions
import org.solyton.solawi.bid.module.bid.data.toDomainType

val deleteAuctionAction: (Auction)->Action<BidApplication, DeleteAuctions, ApiAuctions> by lazy {
    { auction: Auction -> Action<BidApplication, DeleteAuctions, ApiAuctions>(
        name = "DeleteAuction",
        reader = { DeleteAuctions(listOf(auction.auctionId)) },
        endPoint = DeleteAuctions::class,
        writer = auctions.set contraMap { apiAuctions: ApiAuctions -> apiAuctions.toDomainType() }
    ) }
}
