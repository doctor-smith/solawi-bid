package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias ShareTypesTable = ShareTypes
typealias ShareTypeEntity = ShareType

object ShareTypes : AuditableUUIDTable("share_types") {
    val providerId = uuid("provider_id")
    val key = varchar("key", 10)
    val name = varchar("name", 250)
    val description = varchar("description", 5000).default("")

    init {
        uniqueIndex(providerId, name)
        uniqueIndex("ux_provider_key",providerId, key)
    }
}

class ShareType(id : EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<ShareType>(ShareTypes)

    var providerId by ShareTypes.providerId
    var name by ShareTypes.name
    var key by ShareTypes.key
    var description by ShareTypes.description

    val shareOffers by ShareOfferEntity referrersOn ShareOffers.shareTypeId

    override var createdAt: DateTime by ShareTypes.createdAt
    override var createdBy: UUID by ShareTypes.createdBy
    override var modifiedAt: DateTime? by ShareTypes.modifiedAt
    override var modifiedBy: UUID? by ShareTypes.modifiedBy
}

