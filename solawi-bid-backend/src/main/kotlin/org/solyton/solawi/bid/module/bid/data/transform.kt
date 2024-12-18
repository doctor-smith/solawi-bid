package org.solyton.solawi.bid.module.bid.data

import kotlinx.datetime.LocalDate
import org.solyton.solawi.bid.module.bid.data.api.Auction
import org.solyton.solawi.bid.module.bid.data.api.BidRound
import org.solyton.solawi.bid.module.bid.data.api.Round
import org.solyton.solawi.bid.module.db.schema.Auction as AuctionEntity
import org.solyton.solawi.bid.module.db.schema.BidRound as BidRoundEntity
import org.solyton.solawi.bid.module.db.schema.Round as RoundEntity

fun List<AuctionEntity>.toApiType(): List<Auction> = map {
    it.toApiType()
}

fun AuctionEntity.toApiType(): Auction = Auction(
    id = id.value.toString(),
    name = name,
    date = LocalDate(date.year, date.monthOfYear, date.dayOfMonth),
    rounds = try{ rounds.map {
        it.toApiType()
    }} catch(e:Exception){
        listOf()
    },
    bidderIds = try {
        bidders.toList().map {
            it.id.value.toString()
        }
    }catch(e:Exception){
        listOf()
    }
)

fun RoundEntity.toApiType(): Round = Round(
    id.value.toString(),
    link,
    state
)

fun BidRoundEntity.toApiType(): BidRound = BidRound(
    id.value.toString(),
    round.toApiType(),
    auction.toApiType(),
    amount,

)



