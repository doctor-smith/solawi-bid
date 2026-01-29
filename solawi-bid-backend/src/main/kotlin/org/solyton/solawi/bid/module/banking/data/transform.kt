package org.solyton.solawi.bid.module.banking.data

import org.evoleq.exposedx.joda.toKotlinxWithZone
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYears
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity

/**
 * Transform a list of [FiscalYearEntity]s to [ApiFiscalYears]
 */
fun List<FiscalYearEntity>.toApiType(): ApiFiscalYears = ApiFiscalYears(
    map { it.toApiType() }
)

/**
 * Transform a [FiscalYearEntity] to an [ApiFiscalYear]
 */
fun FiscalYearEntity.toApiType(): ApiFiscalYear = ApiFiscalYear(
    id.value.toString(),
    legalEntityId.toString(),
    start.toKotlinxWithZone().date,
    end.toKotlinxWithZone().date
)
