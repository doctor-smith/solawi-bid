package org.solyton.solawi.bid.module.banking.data

import org.evoleq.exposedx.joda.toKotlinxWithZone
import org.solyton.solawi.bid.module.banking.data.api.ApiAccountType
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYears
import org.solyton.solawi.bid.module.banking.data.api.ApiLegalEntity
import org.solyton.solawi.bid.module.banking.data.api.ApiLegalEntityType
import org.solyton.solawi.bid.module.banking.schema.AccountType
import org.solyton.solawi.bid.module.banking.schema.LegalEntity
import org.solyton.solawi.bid.module.banking.schema.BankAccountEntity
import org.solyton.solawi.bid.module.banking.data.api.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity
import org.solyton.solawi.bid.module.banking.schema.LegalEntityType
import org.solyton.solawi.bid.module.user.data.toApiType
import org.solyton.solawi.bid.module.values.LegalEntityId
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
    iban = IBAN(iban),
    accountHolder = accountHolder,
    isActive = isActive,
    accountType = accountType.toApiType(),
    description = description,
)

fun AccountType.toApiType(): ApiAccountType = when (this) {
    AccountType.DEBTOR -> ApiAccountType.DEBTOR
    AccountType.CREDITOR -> ApiAccountType.CREDITOR
}

fun ApiAccountType.toDomainType(): AccountType = when (this) {
    ApiAccountType.DEBTOR -> AccountType.DEBTOR
    ApiAccountType.CREDITOR -> AccountType.CREDITOR
}

fun LegalEntity.toApiType(): ApiLegalEntity = ApiLegalEntity(
    LegalEntityId(id.value.toString()),
    LegalEntityId(partyId.toString()),
    name,
    legalForm,
    legalEntityType.toApiType(),
    address.toApiType()
)

fun LegalEntityType.toApiType(): ApiLegalEntityType = when (this) {
    LegalEntityType.HUMAN -> ApiLegalEntityType.HUMAN
    LegalEntityType.ORGANIZATION -> ApiLegalEntityType.ORGANIZATION
}

fun CreditorIdentifierEntity.toApiType(): CreditorIdentifier = CreditorIdentifier(
    creditorIdentifierId = CreditorIdentifierId(id.value.toString()),
    legalEntityId = LegalEntityId(legalEntity.id.value.toString()),
    creditorId = CreditorId(creditorId),
    validFrom = validFrom.toKotlinxWithZone().date,
    validUntil= validUntil ?.toKotlinxWithZone()?.date,
    isActive = isActive
)
