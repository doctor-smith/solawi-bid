package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias LifecycleTransitionsTable = LifecycleTransitions
typealias LifecycleTransitionEntity = LifecycleTransition

object LifecycleTransitions : AuditableUUIDTable("lifecycle_transitions") {
    val fromId = reference("from", LifecycleStages.id)
    val toId = reference("to", LifecycleStages.id)
    val description = text("description")
}

class LifecycleTransition(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object: UUIDEntityClass<LifecycleTransition> (LifecycleTransitions)

    var from by LifecycleStage referencedOn LifecycleTransitions.fromId
    var to by LifecycleStage referencedOn LifecycleTransitions.toId
    var description by LifecycleTransitions.description

    override var createdAt: DateTime by LifecycleTransitions.createdAt
    override var createdBy: UUID by LifecycleTransitions.createdBy
    override var modifiedAt: DateTime? by LifecycleTransitions.modifiedAt
    override var modifiedBy: UUID? by LifecycleTransitions.modifiedBy
}
