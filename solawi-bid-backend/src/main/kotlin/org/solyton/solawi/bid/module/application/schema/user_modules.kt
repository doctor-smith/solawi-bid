package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

object UserModules : AuditableUUIDTable("user_modules") {
    val userId = uuid("user_id")
    val moduleId = reference("module_id", Modules.id)
    val lifecycleStageId = reference("lifecycle_stage_id", LifecycleStagesTable.id)

    init {
        uniqueIndex(userId, moduleId)
    }
}

class UserModule(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object: UUIDEntityClass<UserModule>(UserModules)

    var userId by UserModules.userId
    var module by Module referencedOn UserModules.moduleId
    var lifecycleStage by LifecycleStageEntity referencedOn UserModules.lifecycleStageId

    override var createdAt: DateTime by UserModules.createdAt
    override var createdBy: UUID by UserModules.createdBy
    override var modifiedAt: DateTime? by UserModules.modifiedAt
    override var modifiedBy: UUID? by UserModules.modifiedBy
}

