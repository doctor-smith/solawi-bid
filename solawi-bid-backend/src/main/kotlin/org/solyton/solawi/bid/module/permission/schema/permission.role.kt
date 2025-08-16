package org.solyton.solawi.bid.module.permission.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias RoleEntity = Role
typealias RolesTable = Roles

object Roles : AuditableUUIDTable("roles") {
    val name = varchar("name",50).uniqueIndex()
    val description = varchar("description", 1000)
}

class Role(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID>{
    companion object : UUIDEntityClass<Role> (Roles)

    var name by Roles.name
    var description by Roles.description

    var contexts by Context via RoleRightContexts
    var rights by Right via RoleRightContexts

    override var createdAt: DateTime by Roles.createdAt
    override var createdBy: UUID by Roles.createdBy
    override var modifiedAt: DateTime? by Roles.modifiedAt
    override var modifiedBy: UUID? by Roles.modifiedBy
}
