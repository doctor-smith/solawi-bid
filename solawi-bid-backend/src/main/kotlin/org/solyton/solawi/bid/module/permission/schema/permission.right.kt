package org.solyton.solawi.bid.module.permission.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias RightEntity = Right
typealias RightsTable = Rights

object Rights : AuditableUUIDTable("rights") {
    val name = varchar("name", 50).uniqueIndex()
    val description=  varchar("description", 1000)
}

class Right(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Right>(Rights)
    // attributes
    var name by Rights.name
    var description by Rights.description
    // navigation
    var roles by Role via RoleRightContexts
    var contexts by Context via RoleRightContexts

    override var createdAt: DateTime by Rights.createdAt
    override var createdBy: UUID by Rights.createdBy
    override var modifiedAt: DateTime? by Rights.modifiedAt
    override var modifiedBy: UUID? by Rights.modifiedBy
}
