package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.markModifiedBy
import org.solyton.solawi.bid.module.banking.exception.FiscalYearException
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity
import org.solyton.solawi.bid.module.banking.schema.FiscalYearsTable
import java.util.*

/**
 * Create a fiscal year
 */
fun Transaction.createFiscalYear(
    legalEntityId: UUID,
    startDate: DateTime,
    endDate: DateTime,
    creator: UUID
): FiscalYearEntity  {
    validateInterval(startDate, endDate)
    validateOverlaps(
        legalEntityId,
        startDate,
        endDate
    )

    return FiscalYearEntity.new {
        createdBy = creator
        this.legalEntityId = legalEntityId
        start = startDate
        end = endDate
    }
}

/**
 * Read all fiscal years on a legal entity
 */
fun Transaction.readFiscalYears(
    legalEntityId: UUID
): List<FiscalYearEntity> = FiscalYearEntity.find {
    FiscalYearsTable.legalEntityId eq legalEntityId
}.toList()

/**
 * Update fiscal year
 */
fun Transaction.updateFiscalYear(
    fiscalYearId: UUID,
    legalEntityId: UUID,
    startDate: DateTime,
    endDate: DateTime,
    modifier: UUID
): FiscalYearEntity {

    val fiscalYear = FiscalYearEntity.findById(fiscalYearId)
        ?: throw FiscalYearException.NoSuchFiscalYear(fiscalYearId.toString())

    val legalEntityChanged = legalEntityId != fiscalYear.legalEntityId
    val intervalChanged = startDate != fiscalYear.start || endDate != fiscalYear.end

    if (intervalChanged) {
        validateInterval(startDate, endDate)
        validateOverlaps(
            legalEntityId,
            startDate,
            endDate,
            excludedFiscalYears = listOf(fiscalYearId)
        )
        fiscalYear.start = startDate
        fiscalYear.end = endDate
    }

    if (legalEntityChanged) {
        fiscalYear.legalEntityId = legalEntityId
    }

    if (intervalChanged) {
        fiscalYear.start = startDate
        fiscalYear.end = endDate
    }

    if (legalEntityChanged || intervalChanged) {
        fiscalYear.markModifiedBy(modifier)
    }

    return fiscalYear
}

/**
 * Delete fiscal year ·
 */
fun Transaction.deleteFiscalYear(fiscalYearId: UUID): UUID {
    FiscalYearsTable.deleteWhere { FiscalYearsTable.id eq fiscalYearId}
    return fiscalYearId;
}

/**
 * Delete fiscal years of a legal entity ·
 */
fun Transaction.deleteFiscalYearsOfLegalEntity(legalEntityId: UUID) {
    FiscalYearsTable.deleteWhere { FiscalYearsTable.legalEntityId eq legalEntityId}
}


fun Transaction.validatedFiscalYear(fiscalYearId: UUID): FiscalYearEntity =
    FiscalYearEntity.findById(fiscalYearId)?: throw FiscalYearException.NoSuchFiscalYear(fiscalYearId.toString())

/**
 * Validate that provided start and end date yield a proper interval
 * (end > start)
 */
fun validateInterval(
    startDate: DateTime,
    endDate: DateTime
) {
    if(endDate <= startDate) throw FiscalYearException.StartAfterEnd
}

/**
 * Validate thar the provided interval does not overlap with other fiscal years associated with the legal entity
 */
fun Transaction.validateOverlaps(
    legalEntityId: UUID,
    startDate: DateTime,
    endDate: DateTime,
    excludedFiscalYears: List<UUID> = listOf()
) {
    val overlapping = FiscalYearEntity.find {
        (FiscalYearsTable.legalEntityId eq legalEntityId) and
        (FiscalYearsTable.id notInList excludedFiscalYears) and
        (
            (FiscalYearsTable.start lessEq endDate and (FiscalYearsTable.start greaterEq startDate)) or
            (FiscalYearsTable.end greaterEq startDate and (FiscalYearsTable.end lessEq endDate))
        )
    }.any()
    if(overlapping) throw FiscalYearException.Overlaps
}


