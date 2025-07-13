package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.AddBidders

fun addBidders(bidders: AddBidders) =
    Action<BidApplication, AddBidders, Unit>(
        name = "AddBidders",
        reader =  Reader{ _ -> bidders },
        endPoint = AddBidders::class,
        writer = {{app->app}}
    )
