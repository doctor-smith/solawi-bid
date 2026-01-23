package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias BundlesTable = Bundles
typealias BundleEntity = Bundle

object Bundles : AuditableUUIDTable("bundles") {
    val name = varchar("name", 100).uniqueIndex()
    val description = text("description")
}

class Bundle(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Bundle>(Bundles)

    var name by Bundles.name
    var description by Bundles.description


    override var createdAt: DateTime by Bundles.createdAt
    override var createdBy: UUID by Bundles.createdBy
    override var modifiedAt: DateTime? by Bundles.modifiedAt
    override var modifiedBy: UUID? by Bundles.modifiedBy
}
