package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias ApplicationsTable = Applications
typealias ApplicationEntity = Application

object Applications : AuditableUUIDTable("applications") {
    val name = varchar("name", 255).uniqueIndex()
    val description = varchar("description", 500)
    val isMandatory = bool("is_mandatory").default(false)

}

class Application(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Application>(Applications)

    var name by Applications.name
    var description by Applications.description
    var isMandatory by Applications.isMandatory
    val modules: SizedIterable<Module> by Module referrersOn Modules.applicationId

    override var createdAt: DateTime by Applications.createdAt
    override var createdBy: UUID by Applications.createdBy
    override var modifiedAt: DateTime? by Applications.modifiedAt
    override var modifiedBy: UUID? by Applications.modifiedBy
}
