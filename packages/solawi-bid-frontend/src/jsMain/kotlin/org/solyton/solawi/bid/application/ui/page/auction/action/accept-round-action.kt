package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.AcceptRound
import org.solyton.solawi.bid.module.bid.data.api.AcceptedRound

@Markup
fun acceptRound(auction: Lens<Application,Auction>, roundId: String): Action<Application, AcceptRound, AcceptedRound> = Action(
    name = "AcceptRound",
    reader = auction * Reader {a:Auction  -> AcceptRound(a.auctionId, roundId) },
    endPoint = AcceptRound::class,
    // The lens acceptedRoundId is ReadOnly
    // -> cannot use contraMap on (auction * acceptedRound).set
    writer = auction * Writer{ acceptedRound: AcceptedRound -> {a -> a.copy(acceptedRoundId = acceptedRound.roundId)} }
)