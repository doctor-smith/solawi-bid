package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias SepaCollectionLinksTable = SepaCollectionLinks
typealias SepaCollectionLinkEntity = SepaCollectionLink

object SepaCollectionLinks: UUIDTable("sepa_collection_links") {
    val predecessorId = reference("predecessor_id", SepaCollectionsTable)
    val successorId = reference("successor_id", SepaCollectionsTable)
    val type = enumerationByName("type", 20, SepaCollectionLinkType::class)
}

enum class SepaCollectionLinkType {
    NEXT_PERIOD,
    // ?
    SPLIT,
    MERGE,
}

class SepaCollectionLink(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SepaCollectionLink>(SepaCollectionLinks)

    var predecessor by SepaCollection referencedOn SepaCollectionLinks.predecessorId
    var successor by SepaCollection referencedOn SepaCollectionLinks.successorId
    var type by SepaCollectionLinks.type
}
