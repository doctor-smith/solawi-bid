package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import java.util.*

typealias RoundEntity = Round
typealias RoundsTable = Rounds

object Rounds: UUIDTable("rounds") {
    val link = varchar("link", 500).default("not-set")
    val state = varchar("state", 100).default("${RoundState.Opened}")
    val auction = reference("auction_id", Auctions)
}


class Round(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Round>(Rounds)

    var link by Rounds.link
    var state by Rounds.state
    var auction by Auction referencedOn Rounds.auction
    val bidRounds by BidRound referrersOn BidRounds.auction
}


