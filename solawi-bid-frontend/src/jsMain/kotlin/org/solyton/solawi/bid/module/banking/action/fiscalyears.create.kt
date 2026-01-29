package org.solyton.solawi.bid.module.banking.action

import kotlinx.datetime.LocalDate
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.CreateFiscalYear
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.toDomainType

/**
 * Creates a fiscal year action for a legal entity with the specified start and end dates.
 *
 * @param legalEntityId The identifier of the legal entity for which the fiscal year is being created.
 * @param start The start date of the fiscal year.
 * @param end The end date of the fiscal year.
 * @param nameSuffix Optional suffix to append to the action name for identification purposes. Defaults to an empty string.
 * @return An `Action` object that defines the creation of the fiscal year within the `BankingApplication` context.
 */
fun createFiscalYear(
    legalEntityId: String,
    start: LocalDate,
    end: LocalDate,
    nameSuffix: String = ""
): Action<BankingApplication, CreateFiscalYear, ApiFiscalYear> = Action(
    name = "CreateFiscalYear$nameSuffix",
    reader = { _ -> CreateFiscalYear(legalEntityId, start, end) },
    endPoint = CreateFiscalYear::class,
    writer = fiscalYears.add() contraMap { fY: ApiFiscalYear -> fY.toDomainType()}
)
