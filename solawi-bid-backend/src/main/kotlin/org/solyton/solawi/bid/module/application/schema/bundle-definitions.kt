package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias BundleDefinitionsTable = BundleDefinitions
typealias BundleDefinitionEntity = BundleDefinition

object BundleDefinitions : AuditableUUIDTable("bundle_definitions") {
    val bundleId = reference("bundle_id", BundlesTable)
    val applicationId =  reference("application_id", ApplicationsTable)
    val moduleId =  reference("module_id", ModulesTable)

    init {
        uniqueIndex(bundleId, applicationId, moduleId)
    }
}

class BundleDefinition(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<BundleDefinition>(BundleDefinitions)

    var bundle by BundleDefinitions.bundleId
    var application by BundleDefinitions.applicationId
    var module by BundleDefinitions.moduleId

    override var createdAt: DateTime by BundleDefinitions.createdAt
    override var createdBy: UUID by BundleDefinitions.createdBy
    override var modifiedAt: DateTime? by BundleDefinitions.modifiedAt
    override var modifiedBy: UUID? by BundleDefinitions.modifiedBy
}
