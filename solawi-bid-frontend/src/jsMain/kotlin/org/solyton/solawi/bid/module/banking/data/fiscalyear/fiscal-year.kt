package org.solyton.solawi.bid.module.banking.data.fiscalyear

import kotlinx.datetime.LocalDate
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite

@Lensify
data class FiscalYear(
    @ReadOnly val fiscalYearId: String,
    @ReadWrite val legalEntityId: String,
    @ReadWrite val start: LocalDate,
    @ReadWrite val end: LocalDate
) {
    companion object {
        val default = FiscalYear("", "", LocalDate(0, 1, 1), LocalDate(0, 12, 31))
    }
}

fun FiscalYear.format(): String = when{
    start.year == end.year -> "${start.year}"
    else -> "${start.year}/${end.year}"
}
