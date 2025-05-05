package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.id.UUIDTable


object RoleRightContexts : UUIDTable("role_right_contexts") {
    val roleId = reference("role_id", Roles)
    val rightId = reference("right_id", Rights)
    val contextId = reference("context_id", Contexts)

    init {
        index(true, roleId, rightId, contextId)
    }
}
