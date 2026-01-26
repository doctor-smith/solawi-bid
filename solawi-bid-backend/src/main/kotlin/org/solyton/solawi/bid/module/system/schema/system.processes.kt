package org.solyton.solawi.bid.module.system.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

typealias SystemProcessesTable = SystemProcesses
typealias SystemProcessEntity = SystemProcess

object SystemProcesses: UUIDTable("system_processes") {
    val name = varchar("name", 100).uniqueIndex()
    val description = text("description")
}

class SystemProcess(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<SystemProcess>(SystemProcesses)

    var name by SystemProcesses.name
    var description by SystemProcesses.description
}
