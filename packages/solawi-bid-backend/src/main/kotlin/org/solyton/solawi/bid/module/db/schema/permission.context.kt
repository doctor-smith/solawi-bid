package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias ContextEntity = Context
typealias ContextsTable = Contexts

object Contexts : UUIDTable("contexts") {
    val name = varchar("name",500).uniqueIndex()
}

class Context(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Context>(Contexts)
    // attributes
    var name by Contexts.name
    // navigation
    var roles by Role via RoleRightContexts
    var rights by Right via RoleRightContexts

    val resources by Resource referrersOn  Resources.contextId
}