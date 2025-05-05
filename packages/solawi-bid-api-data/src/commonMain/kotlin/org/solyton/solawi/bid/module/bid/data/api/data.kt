package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.bid.data.validation.ValidationException

typealias ApiBid = Bid
typealias ApiNewBidder = NewBidder
typealias ApiBidder = Bidder
typealias ApiBidderInfo = BidderInfo
typealias ApiBidRound = BidRound
typealias ApiRound = Round
typealias ApiAuction = Auction
typealias ApiAuctions = Auctions
typealias ApiAuctionDetails = AuctionDetails
typealias ApiAuctionDetailsSolawiTuebingen = AuctionDetails.SolawiTuebingen
typealias ApiBidInfo = BidInfo
typealias ApiBidRoundResults = BidRoundResults
typealias ApiBidResult = BidResult
typealias ApiAcceptedRound = AcceptedRound
typealias ApiAcceptRound = AcceptRound

@Serializable
data class Bid(
    val username: String,
    val link: String,
    val amount: Double
)

@Serializable
data class BidInfo(
    val amount: Double,
    val numberOfShares: Int
)

@Serializable
data class NewBidder(
    val username: String,
    val weblingId: Int,
    val numberOfShares: Int
) {
    init {
       // if(numberOfParts < 0) throw BidRoundException.IllegalNumberOfParts(numberOfParts)
    }
}

@Serializable
data class Bidder(
    //@Serializable(with = UUIDSerializer::class)
    val id: String,
    val username: String,
    val weblingId: Int,
    val numberOfParts: Int,
    val bidRounds: List<BidRound> = listOf()
) {
    init {
       // if(numberOfParts < 0) throw BidRoundException.IllegalNumberOfParts(numberOfParts)
    }
}

@Serializable
data class BidderInfo(
    val id: String,
    val numberOfShares: Int,
)

@Serializable
data class  PreRound(
    //@Serializable(with = UUIDSerializer::class)
    val roundId: String//Uuid,
)

@Serializable
data class  ChangeRoundState(
    //@Serializable(with = UUIDSerializer::class)
    val roundId: String,// Uuid,
    val state: String
) {
    init {
     //   if(!state.isValidRoundStateName()) throw BidRoundException.NoSuchRoundState(state)
    }
}

@Serializable
data class Round(
    val id: String,
    val link: String,
    val state: String,
    val rawResults: ApiBidRoundResults = ApiBidRoundResults(),
    val bidRoundEvaluation: ApiBidRoundEvaluation = ApiBidRoundEvaluation(),
    val preEvaluation: ApiBidRoundPreEvaluation = ApiBidRoundPreEvaluation()
)

@Serializable
data class CreateRound(
    val auctionId: String
)

@Serializable
data class GetRound(
    val id: String
)


@Serializable
data class CreateAuction(
    val name: String,
    val date: LocalDate
)

@Serializable
data class ConfigureAuction(
    val id: String,
    val name: String,
    val date: LocalDate,
    val auctionDetails: AuctionDetails = AuctionDetails.Empty
)

@Serializable
data class Auction(
    val id: String,
    val name: String,
    val date: LocalDate,
    val rounds: List<Round> = listOf(),
    val bidderInfo: List<BidderInfo> = listOf(),
    val auctionDetails: AuctionDetails = AuctionDetails.Empty,
    val acceptedRoundId: String? = null
)

@Serializable
sealed class AuctionDetails {
    @Serializable
    data object Empty : AuctionDetails()
    @Serializable
    data class SolawiTuebingen(
        val minimalBid: Double,
        val benchmark: Double,
        val targetAmount: Double,
        val solidarityContribution: Double
    ) : AuctionDetails() {
        init {
            when{
                minimalBid < 0.0-> throw ValidationException.AuctionDetailsSolawiTuebingen.ValueOutOfRange
                benchmark < 0.0-> throw ValidationException.AuctionDetailsSolawiTuebingen.ValueOutOfRange
                targetAmount < 0.0-> throw ValidationException.AuctionDetailsSolawiTuebingen.ValueOutOfRange
                solidarityContribution< 0.0 -> throw ValidationException.AuctionDetailsSolawiTuebingen.ValueOutOfRange
            }
        }
    }
}

@Serializable
data object GetAuctions

@Serializable
data class Auctions(
    val list: List<Auction> = listOf()
)

@Serializable
data class UpdateAuctions(
    val list: List<Auction> = listOf()
)

@Serializable
data class DeleteAuctions(
    val auctionIds: List<String>
)


@Serializable
data class BidRound(
    //@Serializable(with = UUIDSerializer::class)
    val id: String, //Uuid, //= ZeroUUID,
    val round: Round,
    val auction: Auction,
    val amount: Double? = null,
    val numberOfShares: Int? = null
)

@Serializable
data class ExportBidRound(
    val roundId: String,
    val auctionId: String
)

@Serializable
data class BidRoundResults(
    val roundId: String = "",
    val results: List<BidResult> = listOf()
)

@Serializable
data class BidResult(
    val username: String,
    val numberOfShares: Int,
    val amount: Double,
    val hasPlacedBid: Boolean
)

@Serializable
data class ImportBidders(
    val auctionId: String,
    val bidders: List<NewBidder>
)

@Serializable
data class DeleteBidders(
    val auctionId: String?,
    val bidderIds: List<String>
)

@Serializable
data class AcceptRound(
    val auctionId: String,
    val roundId: String
)
@Serializable
data class AcceptedRound(
    // val auctionId: String,
    val roundId: String
)
