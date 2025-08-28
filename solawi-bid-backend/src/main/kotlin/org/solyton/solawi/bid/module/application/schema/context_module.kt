package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.id.UUIDTable
import org.solyton.solawi.bid.module.permission.schema.ContextsTable

typealias ModuleContextsTable = ModuleContexts

object ModuleContexts : UUIDTable("module_contexts") {
    val moduleId = reference("module_id", ModulesTable)
    val contextId = reference("context_id", ContextsTable)
}
