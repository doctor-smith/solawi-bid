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

typealias OrganizationModuleContextsTable = OrganizationModuleContexts
typealias OrganizationModuleContextEntity = OrganizationModuleContext

object OrganizationModuleContexts : AuditableUUIDTable("organization_module_contexts") {
    val organizationId = uuid("organization_id")
    val moduleId = reference("module_id", Modules.id)
    val lifecycleStageId = reference("lifecycle_stage_id", LifecycleStagesTable.id)
    val contextId = reference("context_id", ContextsTable)

    init {
        uniqueIndex(organizationId, moduleId, contextId)
    }
}

class OrganizationModuleContext(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<OrganizationModuleContext>(OrganizationModuleContexts)

    var organizationId by OrganizationModuleContexts.organizationId
    var module by Module referencedOn OrganizationModuleContexts.moduleId
    var lifecycleStage by LifecycleStageEntity referencedOn OrganizationModuleContexts.lifecycleStageId
    var context by ContextEntity referencedOn OrganizationModuleContexts.contextId

    override var createdAt: DateTime by OrganizationModuleContexts.createdAt
    override var createdBy: UUID by OrganizationModuleContexts.createdBy
    override var modifiedAt: DateTime? by OrganizationModuleContexts.modifiedAt
    override var modifiedBy: UUID? by OrganizationModuleContexts.modifiedBy
}
