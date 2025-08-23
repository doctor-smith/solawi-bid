package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias LifecycleStagesTable = LifecycleStages
typealias LifecycleStageEntity = LifecycleStage


object LifecycleStages : AuditableUUIDTable("lifecycle_stages") {
    val name = varchar("name", 50).uniqueIndex()
    val description = text("description")
}

class LifecycleStage(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object: UUIDEntityClass<LifecycleStage>(LifecycleStages)

    var name by LifecycleStages.name
    var description by LifecycleStages.description

    override var createdAt: DateTime by LifecycleStages.createdAt
    override var createdBy: UUID by LifecycleStages.createdBy
    override var modifiedAt: DateTime? by LifecycleStages.modifiedAt
    override var modifiedBy: UUID? by LifecycleStages.modifiedBy
}
