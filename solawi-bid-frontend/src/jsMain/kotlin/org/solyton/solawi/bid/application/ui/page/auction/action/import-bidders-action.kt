package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.ImportBidders
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.toDomainType

fun importBidders(newBidders: List<NewBidder>, auction: Lens<Application, Auction>) =
    Action<Application, ImportBidders, ApiAuction>(
        name = "ImportBidders",
        reader = auction * Reader{ a: Auction -> ImportBidders(a.auctionId, newBidders) },
        endPoint = ImportBidders::class,
        writer = auction * Writer{ apiAuction: ApiAuction -> {auction: Auction -> auction.copy(
            name = apiAuction.name,
            date = apiAuction.date,
            rounds = apiAuction.rounds.map{it.toDomainType()},
            bidderIds = apiAuction.bidderIds
        )} }
    )
