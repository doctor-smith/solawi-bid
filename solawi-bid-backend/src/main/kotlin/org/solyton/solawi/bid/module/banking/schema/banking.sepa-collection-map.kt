package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object SepaCollectionMappings : UUIDTable("sepa_collection_mappings") {
    val sepaCollectionId = reference("sepa_collection_id", SepaCollectionsTable)
    val referenceId = uuid("reference_id")

    init{
        uniqueIndex(sepaCollectionId, referenceId)
    }
}

class SepaCollectionMapping(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SepaCollectionMapping>(SepaCollectionMappings)

    var referenceId by SepaCollectionMappings.referenceId
    var sepaCollection by SepaCollection referencedOn SepaCollectionMappings.sepaCollectionId
}
