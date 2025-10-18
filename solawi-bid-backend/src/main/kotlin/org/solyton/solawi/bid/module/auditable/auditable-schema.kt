package org.solyton.solawi.bid.module.auditable

import org.evoleq.exposedx.joda.now
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import java.util.*

/**
 * Implement in exposed tables
 */
interface AuditableColumns<Id> {
    val createdAt: Column<DateTime>
    val createdBy: Column<UUID>
    val modifiedAt: Column<DateTime?>
    val modifiedBy: Column<Id?>
}


abstract class AuditableTable<Id : Comparable<Id>>(name: String) : IdTable<Id>(name) , AuditableColumns<Id>

open class AuditableUUIDTable(
    name: String,
    idColumnName: String = "id"
) : AuditableTable<UUID>(name){
    final override val id: Column<EntityID<UUID>> = uuid(idColumnName).autoGenerate().entityId()
    final override val primaryKey = PrimaryKey(id)
    override val createdAt: Column<DateTime> = datetime("created_at").default(now())
    override val createdBy: Column<UUID> = uuid("created_by")
    override val modifiedAt: Column<DateTime?> = datetime("modified_at").nullable()
    override val modifiedBy: Column<UUID?> = uuid("modified_by").nullable()
}
