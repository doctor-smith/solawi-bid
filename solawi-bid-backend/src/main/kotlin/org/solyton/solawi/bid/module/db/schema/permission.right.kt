package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias RightEntity = Right
typealias RightsTable = Rights

object Rights : UUIDTable("rights") {
    val name = varchar("name", 50).uniqueIndex()
    val description=  varchar("description", 1000)
}

class Right(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Right>(Rights)
    // attributes
    var name by Rights.name
    var description by Rights.description
    // navigation
    var roles by Role via RoleRightContexts
    var contexts by Context via RoleRightContexts
}
