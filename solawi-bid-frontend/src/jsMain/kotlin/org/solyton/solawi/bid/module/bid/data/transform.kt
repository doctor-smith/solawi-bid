package org.solyton.solawi.bid.module.bid.data

import kotlinx.datetime.LocalDate
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.ApiAuctions
import org.solyton.solawi.bid.module.bid.data.api.ApiBidRound
import org.solyton.solawi.bid.module.bid.data.api.ApiRound

fun ApiAuctions.toDomainType(): List<Auction> = list.map { auction -> auction.toDomainType() }

fun ApiRound.toDomainType(): Round = Round(id, link, state)

fun ApiBidRound.toDomainType(): BidRound = BidRound(
    id,
    round.toDomainType(),
    auction.toDomainType(),
    amount
)

fun ApiAuction.toDomainType(): Auction = Auction(
    auctionId = id,
    name = name,
    date = with(date){ LocalDate(year, monthNumber, dayOfMonth) },
    rounds = rounds.map { round -> round.toDomainType() },
    bidderIds = bidderIds
)