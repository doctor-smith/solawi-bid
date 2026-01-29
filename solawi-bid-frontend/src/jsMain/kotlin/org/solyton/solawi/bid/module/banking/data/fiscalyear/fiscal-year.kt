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
)
