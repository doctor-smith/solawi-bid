package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object UserOrganization : UUIDTable("user_organization") {
    val organizationId = reference("organizationId", Organisations, onDelete = ReferenceOption.CASCADE)
    val userId = reference("userId", Users, onDelete = ReferenceOption.CASCADE)

    init{
        uniqueIndex(organizationId, userId)
    }
}

