package org.solyton.solawi.bid.module.permission.schema

import org.jetbrains.exposed.dao.id.UUIDTable

object UserRoleContext: UUIDTable("user_role_context") {
    val userId = uuid("user_id") // reference("user_id", Users)
    val contextId = reference("context_id", Contexts)
    val roleId = reference("role_id", Roles)
}
