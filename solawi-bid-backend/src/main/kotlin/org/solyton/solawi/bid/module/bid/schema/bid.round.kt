package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import java.util.*

typealias RoundEntity = Round
typealias RoundsTable = Rounds

object Rounds: AuditableUUIDTable("rounds") {
    val link = varchar("link", 500).default("not-set")
    val state = varchar("state", 100).default("${RoundState.Opened}")
    val number = integer("number").default(0)
    val auction = reference("auction_id", Auctions)
}


class Round(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Round>(Rounds)

    var link by Rounds.link
    var state by Rounds.state
    var number by Rounds.number
    var auction by Auction referencedOn Rounds.auction
    val bidRounds by BidRound referrersOn BidRounds.auction
    val comments by RoundComment referrersOn RoundComments.roundId

    override var createdAt: DateTime by Rounds.createdAt
    override var createdBy: UUID by Rounds.createdBy
    override var modifiedAt: DateTime? by Rounds.modifiedAt
    override var modifiedBy: UUID? by Rounds.modifiedBy
}


