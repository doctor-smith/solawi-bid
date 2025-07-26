package org.solyton.solawi.bid.module.bid.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.solyton.solawi.bid.module.bid.data.addBidInfo
import org.solyton.solawi.bid.module.bid.data.api.ApiBidRound
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.db.BidRoundException
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.db.schema.BidRound as BidRoundEntity

@MathDsl
val StoreBid: KlAction<Result<Bid>, Result<ApiBidRound>> = KlAction { bid: Result<Bid> ->  DbAction {
    database -> bid bindSuspend  {data -> resultTransaction(database) {

    val bidder = Bidder.find { Bidders.username eq data.username }.firstOrNull()
        ?: throw BidRoundException.UnregisteredBidder(data.username)

    // todo query optimization
    (storeBid(data).toApiType() x (getBidderDetails(bidder) as BidderDetails.SolawiTuebingen).numberOfShares).addBidInfo()
    } } x database
} }

fun Transaction.storeBid(bid: Bid): BidRoundEntity {
    // get corresponding round
    val round = Round.find { Rounds.link eq bid.link }.firstOrNull()
        ?: throw BidRoundException.LinkNotPresent(bid.link)

    validateAuctionNotAccepted(round)

    val bidder = Bidder.find { Bidders.username eq bid.username }.firstOrNull()
        ?: throw BidRoundException.UnregisteredBidder(bid.username)

    if(round.auction.bidders.none { it.id.value == bidder.id.value })
        throw BidRoundException.RegisteredBidderNotPartOfTheAuction(bid.username)


    // validate round status
    if(round.state != "${RoundState.Started}" )
        throw BidRoundException.RoundNotStarted

    // update bid-round if present,
    // create a new one otherwise
    val bidRound = BidRoundEntity.find {
        (BidRounds.auction eq round.auction.id.value) and
        (BidRounds.round eq round.id.value) and
        (BidRounds.bidder eq bidder.id.value)
    }.firstOrNull()
    return if(bidRound != null) {
        bidRound.amount = bid.amount
        bidRound
    }
    else {
        BidRoundEntity.new{
            auction = round.auction
            this.round = round
            amount = bid.amount
            this.bidder = bidder
        }
    }
}
