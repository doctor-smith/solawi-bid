package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object SepaMandateCollectionsTable : UUIDTable("sepa_mandate_collections") {
    val sepaMandateId = reference("sepa_mandate_id", SepaMandates)
    val sepaCollectionId = reference("sepa_collection_id", SepaCollections)

    init {
        uniqueIndex(sepaMandateId, sepaCollectionId)
    }
}

class SepaMandateCollectionEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SepaMandateCollectionEntity>(SepaMandateCollectionsTable)

    var sepaMandate by SepaMandate referencedOn SepaMandateCollectionsTable.sepaMandateId
    var sepaCollection by SepaCollection referencedOn SepaMandateCollectionsTable.sepaCollectionId
}
