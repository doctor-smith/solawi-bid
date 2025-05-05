package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.CreateAuction

fun createAuction(auction: Lens<Application, Auction >) =
    Action<Application, CreateAuction, ApiAuction>(
        name = "CreateAuction",
        reader = auction * Reader{ a: Auction -> CreateAuction(a.name, a.date) },
        endPoint = CreateAuction::class,
        writer = auction * Writer{ apiAuction: ApiAuction -> {auction: Auction -> auction.copy(auctionId = apiAuction.id)} }
    )
