package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

object UserApplications : AuditableUUIDTable("user_applications") {
    val userId = uuid("user_id")
    val applicationId = reference("application_id", Applications.id)
    val lifecycleStageId = reference("lifecycle_stage_id", LifecycleStagesTable.id)

    init {
        uniqueIndex(userId, applicationId)
    }
}

class UserApplication(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object: UUIDEntityClass<UserApplication>(UserApplications)

    var userId by UserApplications.userId
    var application by Application referencedOn UserApplications.applicationId
    var lifecycleStage by LifecycleStageEntity referencedOn UserApplications.lifecycleStageId

    override var createdAt: DateTime by UserApplications.createdAt
    override var createdBy: UUID by UserApplications.createdBy
    override var modifiedAt: DateTime? by UserApplications.modifiedAt
    override var modifiedBy: UUID? by UserApplications.modifiedBy
}
