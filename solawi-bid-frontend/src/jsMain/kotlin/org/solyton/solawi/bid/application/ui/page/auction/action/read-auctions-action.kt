package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.merge
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.bid.data.api.ApiAuctions
import org.solyton.solawi.bid.module.bid.data.api.GetAuctions
import org.solyton.solawi.bid.module.bid.data.toDomainType
import org.solyton.solawi.bid.module.user.data.User

@Markup
fun readAuctions(): Action<Application, GetAuctions, ApiAuctions> = Action(
    name ="ReadAuctions",
    reader = userData * Reader { user: User -> GetAuctions },
    endPoint = GetAuctions::class,
    writer = auctions
        merge { given, incoming -> given.auctionId == incoming.auctionId }
        contraMap { apiAuctions: ApiAuctions -> apiAuctions.toDomainType() }
)