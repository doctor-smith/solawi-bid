package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.id.UUIDTable
import org.solyton.solawi.bid.module.permission.schema.Contexts
import org.solyton.solawi.bid.module.permission.schema.Roles

object UserRoleContext: UUIDTable("user_role_context") {
    val userId = reference("user_id", Users)
    val contextId = reference("context_id", Contexts)
    val roleId = reference("role_id", Roles)
}
