package org.solyton.solawi.bid.module.permission.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias ResourceEntity = Resource
typealias ResourcesEntity = Resources

object Resources : AuditableUUIDTable("resources") {
    val name = varchar("name", 255)
    // val type = varchar("type", 500)
    val contextId = reference("context_id", Contexts)
}

class Resource(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Resource>(Resources)

    var name by Resources.name
    var context by Context referencedOn Contexts.id

    override var createdAt: DateTime by Resources.createdAt
    override var createdBy: UUID by Resources.createdBy
    override var modifiedAt: DateTime? by Resources.modifiedAt
    override var modifiedBy: UUID? by Resources.modifiedBy
}
