package org.solyton.solawi.bid.module.bid.data.reader

import org.evoleq.math.Reader
import org.evoleq.math.map
import org.evoleq.uuid.isUuid
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.AuctionDetails
import org.solyton.solawi.bid.module.bid.data.Round
import org.solyton.solawi.bid.module.bid.data.api.RoundState

val auctionAccepted: Reader<Auction, Boolean> = Reader {
    it.acceptedRoundId != null && it.acceptedRoundId.isUuid()
}

val roundAccepted: (String)-> Reader<Auction, Boolean> = {roundId:String -> Reader {
    it.acceptedRoundId == roundId
} }

val countBidders: Reader<Auction, Int> = Reader {
    it.bidderInfo.map{info -> info.bidderId}.distinct().size
}

val biddersHaveNotBeenImported = countBidders map { it <= 0 }

val countShares: Reader<Auction, Int> = Reader {
    it.bidderInfo.sumOf { info -> info.numberOfShares }
}

val existRounds: Reader<List<Round>, Boolean> = Reader {
        rounds -> rounds.isNotEmpty()
}

val existsRunning: Reader<List<Round>, Boolean> = Reader { rounds ->
    val states = rounds.map { it.state }
    val result = states.contains(RoundState.Opened.toString()) || states.contains(RoundState.Started.toString())
    result
}

val areNotConfigured: Reader<AuctionDetails, Boolean> = Reader { details ->
    details.benchmark == null ||
        details.solidarityContribution == null ||
        details.minimalBid == null ||
        details.targetAmount  == null
}
