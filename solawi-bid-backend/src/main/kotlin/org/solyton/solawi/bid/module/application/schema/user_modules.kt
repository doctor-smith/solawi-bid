package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import java.util.*

typealias UserModulesTable = UserModules
typealias UserModuleEntity = UserModule

object UserModules : AuditableUUIDTable("user_modules") {
    val userId = uuid("user_id")
    val moduleId = reference("module_id", Modules.id)
    val lifecycleStageId = reference("lifecycle_stage_id", LifecycleStagesTable.id)
    val contextId = reference("context_id", ContextsTable)

    init {
        uniqueIndex(userId, moduleId, contextId)
    }
}

class UserModule(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object: UUIDEntityClass<UserModule>(UserModules)

    var userId by UserModules.userId
    var module by Module referencedOn UserModules.moduleId
    var lifecycleStage by LifecycleStageEntity referencedOn UserModules.lifecycleStageId
    var context by ContextEntity referencedOn UserModules.contextId

    override var createdAt: DateTime by UserModules.createdAt
    override var createdBy: UUID by UserModules.createdBy
    override var modifiedAt: DateTime? by UserModules.modifiedAt
    override var modifiedBy: UUID? by UserModules.modifiedBy
}

