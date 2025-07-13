package org.solyton.solawi.bid.module.bid.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.merge
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiAuctions
import org.solyton.solawi.bid.module.bid.data.api.GetAuctions
import org.solyton.solawi.bid.module.bid.data.auctions
import org.solyton.solawi.bid.module.bid.data.toDomainType
import org.solyton.solawi.bid.module.bid.data.biduser.User
import org.solyton.solawi.bid.module.bid.data.user

@Markup
fun readAuctions(): Action<BidApplication, GetAuctions, ApiAuctions> = Action(
    name ="ReadAuctions",
    reader = user * Reader { _: User -> GetAuctions },
    endPoint = GetAuctions::class,
    writer = auctions
        merge { given, incoming -> given.auctionId == incoming.auctionId }
        contraMap { apiAuctions: ApiAuctions -> apiAuctions.toDomainType() }
)
