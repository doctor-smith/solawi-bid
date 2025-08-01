package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.merge
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiBid
import org.solyton.solawi.bid.module.bid.data.api.ApiBidRound
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.bidRounds
import org.solyton.solawi.bid.module.bid.data.toDomainType


val sendBidAction: (Bid)-> Action<BidApplication, ApiBid, ApiBidRound> by lazy { { bid ->
    Action<BidApplication, ApiBid, ApiBidRound>(
        name = "SendBid",
        reader = { ApiBid(bid.username, bid.link ,bid.amount) },
        endPoint = Bid::class,
        writer = bidRounds
            merge{ given, incoming -> given.bidRoundId == incoming.bidRoundId }
            contraMap { apiBidRound: ApiBidRound -> listOf(apiBidRound.toDomainType(true)) }
    )
} }
