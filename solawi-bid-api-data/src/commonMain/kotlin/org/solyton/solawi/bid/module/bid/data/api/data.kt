package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

typealias ApiBid = Bid
typealias ApiNewBidder = NewBidder
typealias ApiBidder = Bidder
typealias ApiBidRound = BidRound
typealias ApiRound = Round
typealias ApiAuction = Auction
typealias ApiAuctions = Auctions


@Serializable
data class Bid(
    val username: String,
    val link: String,
    val amount: Double
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
    //@Serializable(with = UUIDSerializer::class)
    val id: String,//Uuid,
    val link: String,
    val state: String
)

@Serializable
data class CreateAuction(
    val name: String,
    val date: LocalDate
)

@Serializable
data class Auction(
    //@Serializable(with = UUIDSerializer::class)
    val id: String,
    val name: String,
    val date: LocalDate,
    val rounds: List<Round> = listOf(),
    val bidderIds: List</*@Serializable(with = UUIDSerializer::class) Uuid*/ String> = listOf()
)

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
    val amount: Double?
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

