package org.solyton.solawi.bid.module.bid.data

import kotlinx.datetime.LocalDate
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.bidder.BidderInfo
import org.solyton.solawi.bid.module.bid.data.evaluation.BidRoundEvaluation
import org.solyton.solawi.bid.module.bid.data.evaluation.BidRoundPreEvaluation
import org.solyton.solawi.bid.module.bid.data.evaluation.WeightedBid


fun ApiAuctions.toDomainType(): List<Auction> = list.map { auction -> auction.toDomainType() }

fun ApiRound.toDomainType(): Round = Round(
    id,
    link,
    state,
    rawResults.toDomainType(),
    bidRoundEvaluation.toDomainType(),
    preEvaluation.toDomainType()

)

fun ApiBidRound.toDomainType(showSuccessMessage: Boolean = false): BidRound = BidRound(
    id,
    showSuccessMessage,
    round.toDomainType(),
    auction.toDomainType(),
    amount,
    numberOfShares
)

fun ApiAuction.toDomainType(): Auction = Auction(
    auctionId = id,
    name = name,
    date = with(date){ LocalDate(year, monthNumber, dayOfMonth) },
    rounds = rounds.map { round -> round.toDomainType() },
    bidderInfo = bidderInfo.map { info -> BidderInfo(
        info.id,
        info.numberOfShares
    ) },
    auctionDetails = auctionDetails.toDomainType(),
    acceptedRoundId = acceptedRoundId
)



fun ApiAuctionDetails.toDomainType(): AuctionDetails  = when(this) {
    is ApiAuctionDetailsSolawiTuebingen -> AuctionDetails(
        minimalBid,
        benchmark,
        targetAmount,
        solidarityContribution
    )
    else -> AuctionDetails()
}

fun ApiBidRoundResults.toDomainType(startDownloadOfBidRoundResults: Boolean = false): BidRoundResults = BidRoundResults(
    results.map { it.toDomainType() },
    startDownloadOfBidRoundResults
)

fun ApiBidResult.toDomainType(): BidResult = BidResult(
    username,
    numberOfShares,
    amount,
    hasPlacedBid
)

fun ApiBidRoundEvaluation.toDomainType(): BidRoundEvaluation = BidRoundEvaluation(
    auctionDetails.toDomainType(),
    totalSumOfWeightedBids,
    totalNumberOfShares,
    weightedBids.map{it.toDomainType()}
)

fun ApiWeightedBid.toDomainType(): WeightedBid = WeightedBid(
    weight,
    bid
)

fun ApiBidRoundPreEvaluation.toDomainType(): BidRoundPreEvaluation = BidRoundPreEvaluation(
    auctionDetails.toDomainType(),
    totalNumberOfShares
)
