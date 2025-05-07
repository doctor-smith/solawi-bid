package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias OrganizationsTable = Organisations
typealias OrganizationEntity = Organization

object Organisations : UUIDTable("organizations") {
    val name = varchar("name", 255).uniqueIndex()
    val rootId = optReference("root_id", Organisations)
    val contextId = reference("context_id", Contexts)
    // sub organizations (parent id, tree left - right)
    // parent id: compute direct parent child relations efficiently
    // val parentId = optReference("parent_id", Organisations)

    // children c of an org o will be characterized by:
    // c.left > o.left
    // c.right < o.right
    val left = integer("left").index().default(0)
    val right = integer("right").index().default(1)
    val level = integer("level").index().default(0)
    // partner organizations (mapping table)

    // members

    // contact person

    // bank account ...


}

class Organization(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Organization>(Organisations)

    var root by OrganizationEntity optionalReferencedOn OrganizationsTable.rootId

    var name by Organisations.name

    var context by ContextEntity referencedOn OrganizationsTable.contextId

    // val parent by OrganizationEntity optionalReferencedOn OrganizationsTable.parentId
    var left by Organisations.left
    var right by Organisations.right
    var level by Organisations.level

    var users by User via UserOrganization
}