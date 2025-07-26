package org.solyton.solawi.bid.module.bid.action.db

import kotlinx.coroutines.coroutineScope
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.db.BidRoundException
import org.solyton.solawi.bid.module.db.schema.*
import java.util.*

@MathDsl
val ExportResults = KlAction<Result<ExportBidRound>, Result<BidRoundResults>> {
    roundData -> DbAction {
        database -> coroutineScope { roundData bindSuspend {data -> resultTransaction(database) {
            getResults(UUID.fromString(data.auctionId),UUID.fromString(data.roundId))
        } } } x database
    }
}

@MathDsl
val EvaluateBidRound = KlAction<Result<EvaluateBidRound>, Result<BidRoundEvaluation>> {
    roundData -> DbAction {
        database -> coroutineScope { roundData bindSuspend {data -> resultTransaction(database) {
            evaluateBidRound(UUID.fromString(data.auctionId),UUID.fromString(data.roundId))
        } } } x database
    }
}

@MathDsl
val PreEvaluateBidRound = KlAction<Result<PreEvaluateBidRound>, Result<BidRoundPreEvaluation>> {
    roundData -> DbAction {
        database -> coroutineScope { roundData bindSuspend {data -> resultTransaction(database) {
            preEvaluateBidRound(UUID.fromString(data.auctionId),UUID.fromString(data.roundId))
        } } } x database
    }
}


fun Transaction.getResults(auctionId: UUID, roundId: UUID):BidRoundResults {
    // Collect auxiliary data
    val auction = AuctionEntity.find {
        AuctionsTable.id eq auctionId
    }.firstOrNull()?: throw BidRoundException.NoSuchAuction

    val bidderDetails = getBidderDetails(auction).map { it as BidderDetails.SolawiTuebingen }

    // Compute results for those who have sent bids
    val bidResults = BidRoundEntity.find {
        BidRoundsTable.round eq roundId
    }.map{
        BidResult(
            it.bidder.username,
            bidderDetails.first { details -> details.bidder.username == it.bidder.username }.numberOfShares,
            it.amount,
            true
        )
    }

    // Compute results for those who did not send bids
    val defaultAmount = when(val auctionDetails = getAuctionDetails(auction)) {
        is AuctionDetails.SolawiTuebingen -> auctionDetails.benchmark + auctionDetails.solidarityContribution
        is AuctionDetails.Empty -> 0.0
    }

    val bidResultUsernames = bidResults.map{it.username}

    val defaultBids = auction.bidders
        .filter { bidder -> !bidResultUsernames.contains( bidder.username) }
        .map {
            BidResult(
                it.username,
                bidderDetails.first { details -> details.bidder.username == it.username }.numberOfShares,
                //it.numberOfShares,
                defaultAmount,
                false
            )
        }

    // Return combined results
    return BidRoundResults(
        roundId.toString(),
        listOf(
            *bidResults.toTypedArray(),
            *defaultBids.toTypedArray()
        )
    )
}

fun Transaction.preEvaluateBidRound(auctionId: UUID, roundId: UUID): BidRoundPreEvaluation {
    // Collect data
    val auction = AuctionEntity.find{AuctionsTable.id eq auctionId}.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction
    val bidRoundResults = getResults(auctionId, roundId)
    val auctionDetails = getAuctionDetails(auction) as AuctionDetails.SolawiTuebingen

    // Start computations
    val totalNumberOfShares = bidRoundResults.results.fold(0) {
            acc, next -> acc + next.numberOfShares
    }

    return BidRoundPreEvaluation(
        auctionDetails = auctionDetails,
        totalNumberOfShares = totalNumberOfShares
    )
}

fun Transaction.evaluateBidRound(auctionId: UUID, roundId: UUID): BidRoundEvaluation {
    // Collect data
    val auction = AuctionEntity.find{AuctionsTable.id eq auctionId}.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction
    val bidRoundResults = getResults(auctionId, roundId)
    val auctionDetails = getAuctionDetails(auction) as AuctionDetails.SolawiTuebingen

    // Start computations.
    // The targetAmount refers to a year. But the bid amounts refer to
    // single shares and months. Thus,
    // to compute the relevant quantity, each share has to be weighted
    // by its corresponding numberOfShares and the sum has to be multiplied
    // by 12.
    val totalSumOfWeightedBids = 12.0 * bidRoundResults.results.fold(0.0) {
        acc, next -> acc + next.numberOfShares.toDouble() * next.amount
    }
    val totalNumberOfShares = bidRoundResults.results.fold(0) {
        acc, next -> acc + next.numberOfShares
    }
    val weightedBids = bidRoundResults.results.map { WeightedBid(it.numberOfShares,it.amount) }

    return BidRoundEvaluation(
        auctionDetails = auctionDetails,
        totalSumOfWeightedBids = totalSumOfWeightedBids,
        totalNumberOfShares = totalNumberOfShares,
        weightedBids = weightedBids
    )
}
