package org.solyton.solawi.bid.module.banking.action

import kotlinx.datetime.LocalDate
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.UpdateFiscalYear
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.toDomainType

/**
 * Updates an existing fiscal year in the banking application for a specified legal entity.
 *
 * @param fiscalYearId The identifier of the fiscal year to be updated.
 * @param legalEntityId The identifier of the legal entity associated with the fiscal year.
 * @param start The updated start date of the fiscal year.
 * @param end The updated end date of the fiscal year.
 * @param nameSuffix Optional suffix to append to the action name for identification purposes. Defaults to an empty string.
 * @return An `Action` object that defines the update process for the fiscal year within the `BankingApplication` context.
 */
fun updateFiscalYear(
    fiscalYearId: String,
    legalEntityId: String,
    start: LocalDate,
    end: LocalDate,
    nameSuffix: String = ""
): Action<BankingApplication, UpdateFiscalYear, ApiFiscalYear> = Action(
    name = "CreateFiscalYear$nameSuffix",
    reader = { _ -> UpdateFiscalYear(
        fiscalYearId,
        legalEntityId,
        start,
        end
    ) },
    endPoint = UpdateFiscalYear::class,
    writer = fiscalYears.update{
        p, q -> p.fiscalYearId == q.fiscalYearId
    } contraMap {
        fY: ApiFiscalYear -> fY.toDomainType()
    }
)
