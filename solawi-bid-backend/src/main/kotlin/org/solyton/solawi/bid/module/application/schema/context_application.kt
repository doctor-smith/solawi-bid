package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.id.UUIDTable
import org.solyton.solawi.bid.module.permission.schema.ContextsTable

typealias ApplicationContextsTable = ApplicationContexts

object ApplicationContexts : UUIDTable("application_contexts") {
    val applicationId = reference("application_id", ApplicationsTable)
    val contextId = reference("context_id", ContextsTable)
}
