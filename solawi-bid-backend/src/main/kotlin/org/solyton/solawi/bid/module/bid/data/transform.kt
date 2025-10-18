package org.solyton.solawi.bid.module.bid.data

import org.evoleq.exposedx.joda.toKotlinxWithZone
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.schema.AcceptedRound
import org.solyton.solawi.bid.module.bid.schema.AcceptedRoundEntity
import org.solyton.solawi.bid.module.bid.schema.AcceptedRoundsTable
import org.solyton.solawi.bid.module.bid.schema.RoundCommentEntity
import org.solyton.solawi.bid.module.bid.schema.Auction as AuctionEntity
import org.solyton.solawi.bid.module.bid.schema.BidRound as BidRoundEntity
import org.solyton.solawi.bid.module.bid.schema.Round as RoundEntity

fun List<AuctionEntity>.toApiType(): List<Auction> = map {
    it.toApiType()
}

fun AuctionEntity.toApiType(): Auction = Auction(
    id = id.value.toString(),
    name = name,
    date = date.toKotlinxWithZone(),
    rounds = try{ rounds.map {
        it.toApiType()
    }} catch(e:Exception){
        listOf()
    },
    bidderInfo = try {
        bidders.toList().map {
            ApiBidderInfo(
                it.id.value.toString(),
                it.numberOfShares
            )
        }
    }catch(e:Exception){
        listOf()
    },
    acceptedRoundId = AcceptedRoundEntity.find{
        AcceptedRoundsTable.auctionId eq id.value
    }.firstOrNull()?.round?.id?.value?.toString()
)

fun RoundEntity.toApiType(): Round = Round(
    id.value.toString(),
    link,
    state,
    number,
    comments = ApiRoundComments(comments.map {it.toApiType()})
)

fun RoundCommentEntity.toApiType(): ApiRoundComment = ApiRoundComment(
    id = id.value.toString(),
    comment = comment,
    createdBy = createdBy.toString(),
    createAt = createdAt.toKotlinxWithZone()
)

@Suppress("UNUSED_PARAMETER")
fun BidRoundEntity.toApiType(fullInfo: Unit? = null): BidRound = BidRound(
    id.value.toString(),
    round.toApiType(),
    auction.toApiType(),
    amount,
    null
)

fun Pair<BidRound, Int>.addBidInfo(): BidRound = first.copy(numberOfShares = second)

fun AcceptedRound.toApiType(): ApiAcceptedRound = ApiAcceptedRound(
    round.id.value.toString()
)
