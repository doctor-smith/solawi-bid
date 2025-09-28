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

typealias UserApplicationsTable = UserApplications
typealias UserApplicationEntity = UserApplication

object UserApplications : AuditableUUIDTable("user_applications") {
    val userId = uuid("user_id")
    val applicationId = reference("application_id", Applications.id)
    val lifecycleStageId = reference("lifecycle_stage_id", LifecycleStagesTable.id)
    val contextId = reference("context_id", ContextsTable)

    init {
        uniqueIndex(userId, applicationId, contextId)
    }
}

class UserApplication(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object: UUIDEntityClass<UserApplication>(UserApplications)

    var userId by UserApplications.userId
    var application by Application referencedOn UserApplications.applicationId
    var lifecycleStage by LifecycleStageEntity referencedOn UserApplications.lifecycleStageId
    var context by ContextEntity referencedOn UserApplications.contextId

    override var createdAt: DateTime by UserApplications.createdAt
    override var createdBy: UUID by UserApplications.createdBy
    override var modifiedAt: DateTime? by UserApplications.modifiedAt
    override var modifiedBy: UUID? by UserApplications.modifiedBy
}
