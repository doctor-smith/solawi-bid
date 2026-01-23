package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias UserBundlesTable = UserBundles
typealias UserBundleEntity = UserBundle

object UserBundles : AuditableUUIDTable("user_bundles") {
    val userId = uuid("user_id")
    val bundleId = reference("bundle_id", BundlesTable)

    init {
        uniqueIndex(userId, bundleId)
    }
}

class UserBundle(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<UserBundle>(UserBundles)

    var user by UserBundles.userId
    var bundle by UserBundles.bundleId

    override var createdAt: DateTime by UserBundles.createdAt
    override var createdBy: UUID by UserBundles.createdBy
    override var modifiedAt: DateTime? by UserBundles.modifiedAt
    override var modifiedBy: UUID? by UserBundles.modifiedBy
}
