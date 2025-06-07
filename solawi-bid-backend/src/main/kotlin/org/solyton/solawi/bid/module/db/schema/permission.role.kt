package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias RoleEntity = Role
typealias RolesTable = Roles

object Roles : UUIDTable("roles") {
    val name = varchar("name",50).uniqueIndex()
    val description = varchar("description", 1000)
}

class Role(id: EntityID<UUID>): UUIDEntity(id){
    companion object : UUIDEntityClass<Role> (Roles)

    var name by Roles.name
    var description by Roles.description

    var contexts by Context via RoleRightContexts
    var rights by Right via RoleRightContexts
}
