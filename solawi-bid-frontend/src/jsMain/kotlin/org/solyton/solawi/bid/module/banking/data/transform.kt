package org.solyton.solawi.bid.module.banking.data

import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYears
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear


/**
 * Converts an instance of ApiFiscalYears to a domain-level representation.
 *
 * Maps each `ApiFiscalYear` item within the `all` list property to its domain
 * type, utilizing the `toDomainType` extension function defined for `ApiFiscalYear`.
 *
 * @receiver ApiFiscalYears instance containing a list of ApiFiscalYear objects to convert.
 * @return A list of domain-level FiscalYear objects.
 */
fun ApiFiscalYears.toDomainType() = all.map { it.toDomainType() }

/**
 * Converts an instance of ApiFiscalYear to its corresponding domain-level representation.
 *
 * Maps the properties of `ApiFiscalYear` to the `FiscalYear` data class, producing
 * a new instance of `FiscalYear` with the same values for `id`, `legalEntityId`,
 * `start`, and `end`.
 *
 * @receiver ApiFiscalYear instance to convert.
 * @return A domain-level FiscalYear object containing the mapped data.
 */
fun ApiFiscalYear.toDomainType() = FiscalYear(id, legalEntityId, start, end)

/**
 * Converts an instance of ApiBankAccounts to a list of BankAccount domain objects.
 * This function maps each ApiBankAccount contained in the `all` property of ApiBankAccounts
 * into its corresponding BankAccount representation.
 *
 * @receiver The ApiBankAccounts instance containing the data to be transformed.
 * @return A list of BankAccount objects derived from the ApiBankAccounts instance.
 */
fun ApiBankAccounts.toDomainType() = all.map { it.toDomainType() }

/**
 * Converts an instance of ApiBankAccount to a BankAccount domain object.
 * This function transforms the API representation of a bank account into its corresponding domain model.
 *
 * @receiver The ApiBankAccount instance containing the data to be transformed.
 * @return A BankAccount object derived from the ApiBankAccount instance.
 */
fun ApiBankAccount.toDomainType() = BankAccount(id,userId,iban, bic )
