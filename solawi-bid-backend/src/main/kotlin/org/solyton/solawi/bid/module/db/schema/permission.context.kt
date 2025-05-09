package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.solyton.solawi.bid.module.db.schema.Organisations.default
import org.solyton.solawi.bid.module.db.schema.Organisations.index
import java.util.*

typealias ContextEntity = Context
typealias ContextsTable = Contexts

object Contexts : UUIDTable("contexts") {
    val name = varchar("name",500).uniqueIndex()

    // Nested Tree structure
    val rootId = optReference("root_id", Contexts)
    val left = integer("left").index().default(0)
    val right = integer("right").index().default(1)
    val level = integer("level").index().default(0)
}

class Context(id: EntityID<UUID>) : UUIDEntity(id) {
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
}