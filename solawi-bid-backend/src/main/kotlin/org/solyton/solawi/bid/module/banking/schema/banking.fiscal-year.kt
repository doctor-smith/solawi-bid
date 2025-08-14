package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.date
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias FiscalYearsTable = FiscalYears
typealias FiscalYearEntity = FiscalYear

object FiscalYears : AuditableUUIDTable("fiscal_years") {

    val start = date("start")
    val end = date("end")

}

class FiscalYear(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<FiscalYear>(FiscalYears)

    var start by FiscalYears.start
    var end by FiscalYears.end

    override var createdAt: DateTime by FiscalYears.createdAt
    override var createdBy: UUID by FiscalYears.createdBy
    override var modifiedAt: DateTime? by FiscalYears.modifiedAt
    override var modifiedBy: UUID? by FiscalYears.modifiedBy
}
