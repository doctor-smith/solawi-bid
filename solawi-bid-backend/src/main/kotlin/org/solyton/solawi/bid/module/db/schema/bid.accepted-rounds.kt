package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias AcceptedRoundsTable = AcceptedRounds
typealias AcceptedRoundEntity = AcceptedRound

object AcceptedRounds : UUIDTable("accepted_rounds") {
    val auctionId = reference("auction_id", AuctionsTable).uniqueIndex()
    val roundId = reference("round_id", RoundsTable).uniqueIndex()
}

class AcceptedRound(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AcceptedRound>(AcceptedRounds)

    var auction by Auction referencedOn  AcceptedRounds.auctionId
    var round by Round referencedOn AcceptedRounds.roundId
}
