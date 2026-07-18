package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object UserOrganization : UUIDTable("user_organization") {
    val organizationId = reference("organizationId", Organisations, onDelete = ReferenceOption.CASCADE)
    val userId = reference("userId", Users, onDelete = ReferenceOption.CASCADE)

    init{
        uniqueIndex(organizationId, userId)
    }
}

class UserOrganizationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserOrganizationEntity>(UserOrganization)

    var organization by Organization referencedOn UserOrganization.organizationId
    var user by User referencedOn UserOrganization.userId
}

