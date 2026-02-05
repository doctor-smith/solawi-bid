package org.solyton.solawi.bid.module.banking.service

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.jodatime.year
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity
import org.solyton.solawi.bid.module.banking.schema.FiscalYearsTable
import java.util.UUID

/**
 * Converts the fiscal year to a simplified string format representing the last two digits
 * of the start year and end year separated by a slash.
 *
 * @return A string in the format "YY/YY", where YY represents the last two digits
 *         of the start and end years of the fiscal year.
 */
fun FiscalYearEntity.toSimpleYearString(): String {
    val startYear = start.year.toString().substring(2)
    val endYear = end.year.toString().substring(2)
    return "$startYear/$endYear"
}

/**
 * Converts the FiscalYear object into a string representation in the format "YYYY/YYYY",
 * where the first year is the start year and the second year is the end year of the fiscal year.
 *
 * @return A string in the format "YYYY/YYYY", representing the fiscal year.
 */
fun FiscalYearEntity.toFullYearString(): String {
    return "${start.year}/${end.year}"
}

/**
 * Identifies the fiscal year associated with the given years and legal entity ID.
 *
 * The input `years` can represent either a single year (e.g., "2023") or a range of years
 * (e.g., "2022/2023"). The method attempts to match this input with the fiscal year data
 * for the specified legal entity ID.
 *
 * @param years A string representing the fiscal year(s) to identify, either in a single year
 * format (e.g., "2023") or a range of years format (e.g., "2022/2023").
 * @param leganEntityId A UUID representing the legal entity for which the fiscal year is to
 * be identified.
 * @return The matching fiscal year entity, or null if no fiscal year matches the given
 * parameters.
 */
fun Transaction.identifyFiscalYear(years: String, leganEntityId: UUID): FiscalYearEntity? {
    val yearsToIdentify = years.split("/").map { it.trim() }

    fun normalizeYear(year: String): Int {
        return when {
            year.length == 2 -> 2000 + year.toInt()
            else -> year.toInt()
        }
    }

    return when (yearsToIdentify.size) {
        1 -> FiscalYearEntity.find {
            (FiscalYearsTable.legalEntityId eq leganEntityId) and
            (FiscalYearsTable.start.year() eq normalizeYear(yearsToIdentify[0]))
        }.firstOrNull()

        2 -> FiscalYearEntity.find {
            FiscalYearsTable.legalEntityId eq leganEntityId and
            (FiscalYearsTable.start.year() eq normalizeYear(yearsToIdentify[0])) and
            (FiscalYearsTable.end.year() eq normalizeYear(yearsToIdentify[1]))
        }.firstOrNull()

        else -> null
    }
}

