package org.solyton.solawi.bid.module.banking.data

import org.evoleq.exposedx.joda.toKotlinxWithZone
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYears
import org.solyton.solawi.bid.module.banking.schema.BankAccountEntity
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity
import org.solyton.solawil.bid.module.bid.data.api.toUserId

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

/**
 * Converts a list of BankAccountEntity instances to their corresponding API type representations.
 *
 * @receiver List of BankAccountEntity objects to be transformed.
 * @return A list of API type representations for the given BankAccountEntity objects.
 */
fun List<BankAccountEntity>.toApiType() = ApiBankAccounts(map { it.toApiType() })

/**
 * Converts a `BankAccountEntity` instance into its corresponding API representation (`ApiBankAccount`).
 *
 * @receiver The `BankAccountEntity` instance to be converted.
 * @return A new `ApiBankAccount` object containing the mapped data.
 */
fun BankAccountEntity.toApiType() = ApiBankAccount(
    id = BankAccountId(id.value.toString()),
    userId = userId.toUserId(),
    bic = BIC(bic),
    iban = IBAN(iban)
)
