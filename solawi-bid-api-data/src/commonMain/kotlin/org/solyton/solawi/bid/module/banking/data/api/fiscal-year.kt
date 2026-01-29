package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.EmptyParams
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams

typealias ApiFiscalYear = FiscalYear
typealias ApiFiscalYears = FiscalYears

@Serializable
data class FiscalYears(
    val all: List<FiscalYear>
)

@Serializable
data class FiscalYear(
    val id: String,
    val legalEntityId: String,
    val start: LocalDate,
    val end: LocalDate
)

@Serializable
data class ReadFiscalYears(
    /**
     * takes param
     * "legal_entity: UUID"
     */
    override val queryParams: QueryParams
) : Parameters()

@Serializable
data class ReadFiscalYear(
    val id: String
)

@Serializable
data class CreateFiscalYear(
    val legalEntityId: String,
    val start: LocalDate,
    val end: LocalDate
)

@Serializable
data class UpdateFiscalYear(
    val id: String,
    val legalEntityId: String,
    val start: LocalDate,
    val end: LocalDate
)

@Serializable
data class DeleteFiscalYear(
    val id: String
)
