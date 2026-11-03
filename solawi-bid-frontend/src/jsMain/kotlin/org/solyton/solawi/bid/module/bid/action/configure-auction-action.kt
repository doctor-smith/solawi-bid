package org.solyton.solawi.bid.module.bid.action

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.ConfigureAuction
import org.solyton.solawi.bid.module.bid.data.auctions
import org.solyton.solawi.bid.module.bid.data.toDomainType

@Markup
fun configureAuction(data: ConfigureAuction) =
    Action<BidApplication, ConfigureAuction, ApiAuction>(
        name = "ConfigureAuction",
        reader = {data },
        endPoint = ConfigureAuction::class,
        writer = auctions.update {
            p, q -> p.auctionId == q.auctionId
        } contraMap {
            apiAuction: ApiAuction -> apiAuction.toDomainType()
        }
    )
