package org.solyton.solawi.bid.module.application.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias ModulesTable = Modules
typealias ModuleEntity = Module

object Modules : AuditableUUIDTable("modules") {
    val name = varchar("name", 255)
    val description = varchar("description", 500)

    val applicationId = reference("application_id", ApplicationsTable.id)

    val isMandatory = bool("is_mandatory").default(false)

    init {
        uniqueIndex(name, applicationId)
    }
}

class Module(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Module>(Modules)

    var name by Modules.name
    var description by Modules.description

    var application by Application referencedOn Modules.applicationId
    var isMandatory by Modules.isMandatory

    override var createdAt: DateTime by Modules.createdAt
    override var createdBy: UUID by Modules.createdBy
    override var modifiedAt: DateTime? by Modules.modifiedAt
    override var modifiedBy: UUID? by Modules.modifiedBy
}
