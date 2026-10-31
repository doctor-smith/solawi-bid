package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.CreateAuction
import org.solyton.solawi.bid.module.bid.data.auctions
import org.solyton.solawi.bid.module.bid.data.toDomainType

fun createAuction(data: CreateAuction) =
    Action<BidApplication, CreateAuction, ApiAuction>(
        name = "CreateAuction",
        reader = Reader{ data },
        endPoint = CreateAuction::class,
        writer = auctions.add() contraMap {
            auction -> auction.toDomainType()
        }
    )
