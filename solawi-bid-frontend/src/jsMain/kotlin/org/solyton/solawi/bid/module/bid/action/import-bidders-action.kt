package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.ImportBidders
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auctions
import org.solyton.solawi.bid.module.bid.data.toDomainType

fun importBidders(newBidders: List<NewBidder>, auction: Lens<BidApplication, Auction>) =
    Action<BidApplication, ImportBidders, ApiAuction>(
        name = "ImportBidders",
        reader = auction * Reader{ a: Auction -> ImportBidders(a.auctionId, newBidders) },
        endPoint = ImportBidders::class,
        writer = auctions.update{
            p, q -> p.auctionId == q.auctionId
        } contraMap { auction -> auction.toDomainType() }
    )
