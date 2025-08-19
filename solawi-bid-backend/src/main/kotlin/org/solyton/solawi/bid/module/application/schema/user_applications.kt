package org.solyton.solawi.bid.module.application.schema

import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable

object UserApplications : AuditableUUIDTable("user_applications") {
    val userId = uuid("user_id")
    val applicationId = reference("application_id", Applications.id)

    init {
        uniqueIndex(userId, applicationId)
    }
}
