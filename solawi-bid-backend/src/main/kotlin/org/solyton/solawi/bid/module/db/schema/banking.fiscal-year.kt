package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.jodatime.date
import java.util.*

typealias FiscalYearsTable = FiscalYears
typealias FiscalYearEntity = FiscalYear

object FiscalYears : UUIDTable("fiscal_years") {

    val start = date("start")
    val end = date("end")

}

class FiscalYear(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FiscalYear>(FiscalYears)

    var start by FiscalYears.start
    var end by FiscalYears.end
}
