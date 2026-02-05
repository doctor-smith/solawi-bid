package org.solyton.solawi.bid.module.banking.service

import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear

/**
 * Converts the fiscal year to a simplified string format representing the last two digits
 * of the start year and end year separated by a slash.
 *
 * @return A string in the format "YY/YY", where YY represents the last two digits
 *         of the start and end years of the fiscal year.
 */
fun FiscalYear.toSimpleYearString(): String {
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
fun FiscalYear.toFullYearString(): String {
    return "${start.year}/${end.year}"
}

/**
 * Identifies a `FiscalYear` in the list based on a string representation of the year(s).
 * The input `years` can represent a single year or a range of years in the format "YYYY", "YY", or "YYYY/YYYY".
 *
 * @param years A string representing the fiscal year or range of fiscal years to identify.
 *              It can be in the format of a single year (e.g., "2023" or "23") or a range of years (e.g., "2022/2023").
 * @return The corresponding `FiscalYear` if found, or `null` if no match exists.
 */
fun List<FiscalYear>.identify(years: String): FiscalYear? {

    val yearsToIdentify = years.split("/").map { it.trim() }

    fun normalizeYear(year: String): Int {
        return when {
            year.length == 2 -> 2000 + year.toInt()
            else -> year.toInt()
        }
    }

    return when (yearsToIdentify.size) {
        1 -> {
            val year = normalizeYear(yearsToIdentify[0])
            find { it.start.year == year || it.end.year == year }
        }

        2 -> {
            val startYear = normalizeYear(yearsToIdentify[0])
            val endYear = normalizeYear(yearsToIdentify[1])
            find { it.start.year == startYear && it.end.year == endYear }
        }

        else -> null
    }
}

