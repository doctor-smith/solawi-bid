package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYears
import org.solyton.solawi.bid.module.banking.data.api.ReadFiscalYears
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.toDomainType

/**
 * Reads the fiscal years from the banking application and updates the application state.
 *
 * @param nameSuffix Optional suffix to append to the action name for identification purposes. Defaults to an empty string.
 * @return An `Action` object configured to read fiscal years from the API and update the application state with domain-level fiscal year data.
 */
fun readFiscalYears(legalEntityId: String, nameSuffix: String = "") = Action<BankingApplication, ReadFiscalYears, ApiFiscalYears>(
    name = "ReadFiscalYears$nameSuffix",
    reader = {_ -> ReadFiscalYears(listOf("legal_entity" to legalEntityId))},
    endPoint = ReadFiscalYears::class,
    writer = fiscalYears.set contraMap { fiscalYears -> fiscalYears.toDomainType() }
)
