package org.solyton.solawi.bid.module.banking.data

import org.evoleq.exposedx.joda.toKotlinxWithZone
import org.jetbrains.exposed.sql.SizedIterable
import org.solyton.solawi.bid.module.banking.data.api.ApiAccountType
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYears
import org.solyton.solawi.bid.module.banking.data.api.ApiLegalEntity
import org.solyton.solawi.bid.module.banking.data.api.ApiLegalEntityType
import org.solyton.solawi.bid.module.banking.data.api.ApiMandateStatus
import org.solyton.solawi.bid.module.banking.data.api.ApiPaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaCollection
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMandate
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaSequenceType
import org.solyton.solawi.bid.module.banking.data.api.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMandates
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayment
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayments
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.user.data.toApiType
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawil.bid.module.bid.data.api.toUserId
import kotlin.let

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

fun SepaCollectionEntity.toApiType(): ApiSepaCollection = ApiSepaCollection(
    sepaCollectionId = SepaCollectionId(id.value.toString()),
    creditorIdentifierId = CreditorIdentifierId(creditorIdentifier.id.value.toString()),
    creditorBankAccountId = BankAccountId(creditorAccount.id.value.toString()),
    mandateReferencePrefix = MandateReferencePrefix(mandateReferencePrefix),
    remittanceInformation = RemittanceInformation(remittanceInformation),
    sepaSequenceType = sequenceType.toApiType(),
    localInstrument = localInstrument?.let{ LocalInstrument(it) },
    chargeBearer = ChargeBearer(chargeBearer),
    requestedCollectionDay = requestedCollectionDay,
    leadTimeDays = leadTimesDays,
    purposeCode = purposeCode?.let { PurposeCode(it) },
    isActive = isActive,
    sepaMandates = sepaMandates.toApiType(),
    sepaPayments = sepaPayments.toApiType(),
    referenceIds = referenceIds.map { SepaCollectionReferenceId(it.referenceId.toString()) }
)

fun SepaSequenceType.toApiType(): ApiSepaSequenceType = when(this) {
    SepaSequenceType.FRST -> ApiSepaSequenceType.FRST
    SepaSequenceType.RCUR -> ApiSepaSequenceType.RCUR
    SepaSequenceType.OOFF -> ApiSepaSequenceType.OOFF
    SepaSequenceType.FNAL -> ApiSepaSequenceType.FNAL
}

fun ApiSepaSequenceType.toDomainType(): SepaSequenceType = when(this) {
    ApiSepaSequenceType.FRST -> SepaSequenceType.FRST
    ApiSepaSequenceType.RCUR -> SepaSequenceType.RCUR
    ApiSepaSequenceType.OOFF -> SepaSequenceType.OOFF
    ApiSepaSequenceType.FNAL -> SepaSequenceType.FNAL
}

fun SizedIterable<SepaMandateEntity>.toApiType(): ApiSepaMandates? = when{
    empty() -> null
    else -> ApiSepaMandates(map { it.toApiType() })
}

fun SepaMandateEntity.toApiType(): ApiSepaMandate = ApiSepaMandate(
    sepaMandateId = SepaMandateId(id.value.toString()),
    debtorBankAccountId = BankAccountId(debtorBankAccount.id.value.toString()),
    debtorName = debtorName,
    mandateReference = MandateReference(mandateReference),
    signedAt = signedAt.toKotlinxWithZone(),
    validFrom = validFrom.toKotlinxWithZone(),
    validUntil = validUntil?.toKotlinxWithZone(),
    lastUsedAt = lastUsedAt?.toKotlinxWithZone(),
    status = status.toApiType(),
    isActive = isActive,
    amendmentOf = amendmentOf?.let { SepaMandateId(it.id.value.toString()) },
    collectionId = collection?.let{ SepaCollectionId(it.id.value.toString()) }
)

fun MandateStatus.toApiType(): ApiMandateStatus = when(this){
    MandateStatus.ACTIVE -> ApiMandateStatus.ACTIVE
    MandateStatus.REVOKED -> ApiMandateStatus.REVOKED
    MandateStatus.EXPIRED -> ApiMandateStatus.EXPIRED
    MandateStatus.SUSPENDED -> ApiMandateStatus.SUSPENDED
}

fun ApiMandateStatus.toDomainType(): MandateStatus = when(this) {
    ApiMandateStatus.ACTIVE -> MandateStatus.ACTIVE
    ApiMandateStatus.REVOKED -> MandateStatus.REVOKED
    ApiMandateStatus.EXPIRED -> MandateStatus.EXPIRED
    ApiMandateStatus.SUSPENDED -> MandateStatus.SUSPENDED
}


fun SizedIterable<SepaPaymentEntity>.toApiType(): ApiSepaPayments? = when{
    empty() -> null
    else -> ApiSepaPayments(map { it.toApiType() })
}

fun SepaPaymentEntity.toApiType(): ApiSepaPayment = ApiSepaPayment(
    sepaPaymentId = SepaPaymentId(id.value.toString()),
    sepaMandateId = SepaMandateId(mandate.id.value.toString()),
    sepaCollectionId = SepaCollectionId(collection.id.value.toString()),
    amount = amount,
    executionDate = executionDate.toKotlinxWithZone().date,
    sequenceType = sequenceType.toApiType(),
    status = status.toApiType(),
    failureReason = failureReason
)

fun PaymentExecutionStatus.toApiType(): ApiPaymentExecutionStatus = when(this){
    PaymentExecutionStatus.CREATED -> ApiPaymentExecutionStatus.CREATED
    PaymentExecutionStatus.SENT -> ApiPaymentExecutionStatus.SENT
    PaymentExecutionStatus.CONFIRMED -> ApiPaymentExecutionStatus.CONFIRMED
    PaymentExecutionStatus.FAILED -> ApiPaymentExecutionStatus.FAILED
    PaymentExecutionStatus.PENDING -> ApiPaymentExecutionStatus.PENDING
}

fun ApiPaymentExecutionStatus.toDomainType(): PaymentExecutionStatus = when(this) {
    ApiPaymentExecutionStatus.CREATED -> PaymentExecutionStatus.CREATED
    ApiPaymentExecutionStatus.SENT -> PaymentExecutionStatus.SENT
    ApiPaymentExecutionStatus.CONFIRMED -> PaymentExecutionStatus.CONFIRMED
    ApiPaymentExecutionStatus.FAILED -> PaymentExecutionStatus.FAILED
    ApiPaymentExecutionStatus.PENDING -> PaymentExecutionStatus.PENDING

}
