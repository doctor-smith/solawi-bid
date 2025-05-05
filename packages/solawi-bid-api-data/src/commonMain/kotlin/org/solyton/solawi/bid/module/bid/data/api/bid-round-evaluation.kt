package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable

typealias ApiBidRoundEvaluation = BidRoundEvaluation
typealias ApiBidRoundPreEvaluation = BidRoundPreEvaluation
typealias ApiWeightedBid = WeightedBid

@Serializable
data class EvaluateBidRound(
    val auctionId: String,
    val roundId: String
)

@Serializable
data class PreEvaluateBidRound(
    val auctionId: String,
    val roundId: String
)

@Serializable
data class BidRoundEvaluation(
    val auctionDetails: AuctionDetails = AuctionDetails.Empty,
    val totalSumOfWeightedBids: Double = 0.0,
    val totalNumberOfShares: Int = 0,
    val weightedBids: List<WeightedBid> = listOf()
)

@Serializable
data class WeightedBid(
    val weight: Int,
    val bid: Double
)

@Serializable
data class BidRoundPreEvaluation(
    val auctionDetails: AuctionDetails = AuctionDetails.Empty,
    val totalNumberOfShares: Int = 0,
)