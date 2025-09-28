package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.and
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import java.util.UUID


fun getDefaultContext(defaultContextId: UUID?): ContextEntity = when(defaultContextId) {
    null -> ContextEntity.find { ContextsTable.rootId eq null and (ContextsTable.name eq "EMPTY") }.firstOrNull()
        ?: throw ContextException.NoSuchContext("EMPTY")
    else -> ContextEntity.find { ContextsTable.id eq defaultContextId }.firstOrNull()
        ?: throw ContextException.NoSuchContext(defaultContextId.toString())
}

fun ApplicationEntity.buildUserApplicationContextName(userId: UUID): String =
    "$name.${id.value}.$userId"

fun ModuleEntity.buildUserModuleContextName(userId: UUID): String =
    "$name.${id.value}.$userId"

/**
 * Drop user and application info
 * Recall: context_name ~ <name>|<name>.<app_id|module_id>.<user_id>
 * Result will return the name
 */
fun String.reduceContextName(): String = with(split(".")) { when{
    size <= 1 -> this@reduceContextName
    size == 2 -> this[0]
    else -> take(size-2).joinToString(".") { it }
} }
