package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias ShareTypesTable = ShareTypes
typealias ShareTypeEntity = Share

object ShareTypes : AuditableUUIDTable("share_types") {
    val name = varchar("name", 250)
    val description = varchar("description", 5000).default("")
    val fixedPrize = double("fixed_price").nullable()
    val ahcAuthorizationRequired = bool("ahc_auth_required").default(false)
    // val currency = varchar("currency", 10).default("Euro") <- import exposed package
}

class ShareType(id : EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<ShareType>(ShareTypes)

    var name by ShareTypes.name
    var description by ShareTypes.description
    var fixedPrize by ShareTypes.fixedPrize
    var ahcAuthorizationRequired by ShareTypes.ahcAuthorizationRequired

    override var createdAt: DateTime by ShareTypes.createdAt
    override var createdBy: UUID by ShareTypes.createdBy
    override var modifiedAt: DateTime? by ShareTypes.modifiedAt
    override var modifiedBy: UUID? by ShareTypes.modifiedBy
}

