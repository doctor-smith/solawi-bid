package org.solyton.solawi.bid.module.application.schema

import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable

object UserModules : AuditableUUIDTable("user_modules") {
    val userId = uuid("user_id")
    val moduleId = reference("module_id", Modules.id)

    init {
        uniqueIndex(userId, moduleId)
    }
}
