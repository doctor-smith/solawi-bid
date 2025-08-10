package org.solyton.solawi.bid.module.auditable

import org.evoleq.uuid.NIL_UUID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.jodatime.date
import org.joda.time.DateTime
import java.util.UUID


interface AuditableColumns<Id> {
    val createdAt: Column<DateTime>
    val createdBy: Column<Id>
    val modifiedAt: Column<DateTime?>
    val modifiedBy: Column<Id?>
}

interface AuditableEntity<Id> {
    val createdAt: DateTime
    val createdBy: Id
    val modifiedAt: DateTime?
    val modifiedBy: Id?
}

abstract class AuditableTable<Id : Comparable<Id>>(open val name: String) : IdTable<Id>(name) , AuditableColumns<Id>

open class AuditableUUIDTable(
    override val name: String,
    idColumnName: String = "id"
) : AuditableTable<UUID>(name){
    final override val id: Column<EntityID<UUID>> = uuid(idColumnName).autoGenerate().entityId()
    final override val primaryKey = PrimaryKey(id)
    override val createdAt: Column<DateTime> = date("created_at").default(DateTime.now())
    override val createdBy: Column<UUID> = uuid("created_by").default(UUID.fromString(NIL_UUID))
    override val modifiedAt: Column<DateTime?> = date("modified_at").nullable()
    override val modifiedBy: Column<UUID?> = uuid("modified_by").nullable()
}
