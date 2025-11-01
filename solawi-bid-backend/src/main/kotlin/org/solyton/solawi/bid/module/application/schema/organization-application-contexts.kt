package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import java.util.UUID

typealias OrganizationApplicationContextsTable = OrganizationApplicationContexts
typealias OrganizationApplicationContextEntity = OrganizationApplicationContext

object OrganizationApplicationContexts : AuditableUUIDTable("organization_application_contexts") {
    val organizationId = uuid("organization_id")
    val applicationId = reference("application_id", Applications.id)
    val lifecycleStageId = reference("lifecycle_stage_id", LifecycleStagesTable.id)
    val contextId = reference("context_id", ContextsTable)

    init {
        uniqueIndex(organizationId, applicationId, contextId)
    }
}

class OrganizationApplicationContext(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<OrganizationApplicationContext>(OrganizationApplicationContexts)

    var organizationId by OrganizationApplicationContexts.organizationId
    var application by Application referencedOn OrganizationApplicationContexts.applicationId
    var lifecycleStage by LifecycleStageEntity referencedOn OrganizationApplicationContexts.lifecycleStageId
    var context by ContextEntity referencedOn OrganizationApplicationContexts.contextId

    override var createdAt: DateTime by OrganizationApplicationContexts.createdAt
    override var createdBy: UUID by OrganizationApplicationContexts.createdBy
    override var modifiedAt: DateTime? by OrganizationApplicationContexts.modifiedAt
    override var modifiedBy: UUID? by OrganizationApplicationContexts.modifiedBy
}
