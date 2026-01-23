package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*


typealias OrganizationBundlesTable = OrganizationBundles
typealias OrganizationBundleEntity = OrganizationBundle

object OrganizationBundles : AuditableUUIDTable("organization_bundles") {
    val organizationId = uuid("organization_id")
    val bundleId = reference("bundle_id", BundlesTable)

    init {
        uniqueIndex(organizationId, bundleId)
    }
}

class OrganizationBundle(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<OrganizationBundle>(OrganizationBundles)

    var organization by OrganizationBundles.organizationId
    var bundle by OrganizationBundles.bundleId

    override var createdAt: DateTime by OrganizationBundles.createdAt
    override var createdBy: UUID by OrganizationBundles.createdBy
    override var modifiedAt: DateTime? by OrganizationBundles.modifiedAt
    override var modifiedBy: UUID? by OrganizationBundles.modifiedBy
}
