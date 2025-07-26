package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias AuctionTypesTable = AuctionTypes
typealias AuctionTypeEntity = AuctionType

object AuctionTypes : UUIDTable("auction_types") {
    val type = varchar("type", 250).uniqueIndex()
}

class AuctionType(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AuctionType>(AuctionTypes)

    var type by AuctionTypes.type

    val auctions by Auction referrersOn Auctions.typeId
}
