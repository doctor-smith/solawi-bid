package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.merge
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.bidRounds
import org.solyton.solawi.bid.module.bid.data.api.ApiBid
import org.solyton.solawi.bid.module.bid.data.api.ApiBidRound
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.toDomainType


val sendBidAction: (Bid)-> Action<Application, ApiBid, ApiBidRound> by lazy { { bid ->
    Action<Application, ApiBid, ApiBidRound>(
        name = "SendBid",
        reader = { ApiBid(bid.username, bid.link ,bid.amount) },
        endPoint = Bid::class,
        writer = bidRounds
            merge{ given, incoming -> given.bidRoundId == incoming.bidRoundId }
            contraMap { apiBidRound: ApiBidRound -> listOf(apiBidRound.toDomainType(true)) }
    )
} }
