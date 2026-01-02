package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

typealias ApiFiscalYear = FiscalYear

@Serializable
data class FiscalYear(
    val id: String,
    val start: LocalDate
)

@Serializable
data object ReadFiscalYears

@Serializable
data class ReadFiscalYear(
    val id: String
)

@Serializable
data class CreateFiscalYear(
    val start: LocalDate,
    val end: LocalDate
)

@Serializable
data class UpdateFiscalYear(
    val id: String,
    val start: LocalDate,
    val end: LocalDate
)

@Serializable
data class DeleteFiscalYear(
    val id: String
)
