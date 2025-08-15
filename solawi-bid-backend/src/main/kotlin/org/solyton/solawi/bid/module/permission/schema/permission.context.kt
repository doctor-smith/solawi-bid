package org.solyton.solawi.bid.module.permission.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias ContextEntity = Context
typealias ContextsTable = Contexts

object Contexts : AuditableUUIDTable("contexts") {
    val name = varchar("name",500).uniqueIndex()

    // Nested Tree structure
    val rootId = optReference("root_id", Contexts)
    val left = integer("left").index().default(0)
    val right = integer("right").index().default(1)
    val level = integer("level").index().default(0)
}

class Context(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Context>(Contexts)
    // attributes
    var name by Contexts.name
    // navigation
    var roles by Role via RoleRightContexts
    var rights by Right via RoleRightContexts

    val resources by Resource referrersOn  Resources.contextId

    var root by ContextEntity optionalReferencedOn ContextsTable.rootId
    var left by Contexts.left
    var right by Contexts.right
    var level by Contexts.level

    override var createdAt: DateTime by Contexts.createdAt
    override var createdBy: UUID by Contexts.createdBy
    override var modifiedAt: DateTime? by Contexts.modifiedAt
    override var modifiedBy: UUID? by Contexts.modifiedBy
}
