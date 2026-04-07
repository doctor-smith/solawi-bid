package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.solyton.solawi.bid.module.banking.data.MandateReference
import org.solyton.solawi.bid.module.banking.data.MandateReferencePrefix
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMandates
import org.solyton.solawi.bid.module.banking.data.api.SepaMandate
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifiers
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifiersTable
import org.solyton.solawi.bid.module.banking.schema.MandateStatus
import org.solyton.solawi.bid.module.banking.schema.SepaMandateEntity
import org.solyton.solawi.bid.module.banking.schema.SepaMandates
import org.solyton.solawi.bid.module.banking.schema.SepaMandatesTable
import org.solyton.solawi.bid.module.banking.service.validatedBankAccount
import java.util.*

/**
 * Creates a SEPA mandate entity with a retry mechanism to handle unique constraint violations
 * during the generation of the mandate reference. The method attempts to insert a new
 * SEPA mandate into the database up to the specified maximum number of retries.
 *
 * @param creditorId The unique identifier of the creditor for whom the mandate is created.
 * @param debtorBankAccountId The unique identifier of the debtor's bank account associated with the mandate.
 * @param debtorName The name of the debtor associated with the bank account.
 * @param signedAt The date and time when the SEPA mandate was signed.
 * @param maxRetries The maximum number of attempts to generate a unique mandate reference. Defaults to 5.
 * @return The newly created SEPA mandate entity.
 * @throws ExposedSQLException If a database error occurs that is not a unique constraint violation.
 * @throws IllegalStateException If unable to generate a unique mandate reference after the specified number of retries.
 */
@Suppress("NoNameShadowing")
fun Transaction.createSepaMandateWithRetry(
    creatorId: UUID,
    creditorId: UUID,
    debtorBankAccountId: UUID,
    debtorName: String,
    signedAt: DateTime,
    validFrom: DateTime,
    validUntil: DateTime?,
    mandateReference: MandateReference? = null,
    mandateReferencePrefix: String? = null,
    status: MandateStatus = MandateStatus.ACTIVE,
    collectionId: UUID? = null,
    maxRetries: Int = 5
): SepaMandateEntity {
    val creditor = validatedCreditor(creditorId)
    val debtorBankAccount = validatedBankAccount(debtorBankAccountId)

    repeat(maxRetries) {
        val mandateReference = mandateReference?.value?: when(mandateReferencePrefix) {
            null -> generateMandateReference(creditorId)
            else -> generateMandateReference(creditorId, 3, mandateReferencePrefix)
        }
        val collection  = if(collectionId != null) {
            validatedSepaCollection(collectionId)
        } else null

        try {
            return SepaMandateEntity.new {
                this.createdBy = creatorId
                this.creditorIdentifier = creditor
                this.debtorBankAccount = debtorBankAccount
                this.debtorName = debtorName
                this.mandateReference = mandateReference
                this.signedAt = signedAt
                this.status = MandateStatus.ACTIVE
                this.validFrom = validFrom
                this.validUntil = validUntil
                this.status = status
                this.isActive = true
                this.collection = collection
            }
        } catch (e: ExposedSQLException) {
            // Check for MySQL unique constraint violation
            if (e.message?.contains("Duplicate entry") == true) {
                // conflict, retry
                return@repeat
            } else {
                throw e
            }
        }
    }
    throw BankAccountsException.CannotCreateMandateReference("Could not generate unique mandate reference after $maxRetries retries")
}

/**
 * Validates a given creditor ID by attempting to find a corresponding `CreditorIdentifierEntity` in the database.
 * If no matching entity is found, a `BankAccountsException.NoSuchCreditorId` exception is thrown.
 *
 * @param creditorId The unique identifier of the creditor to be validated.
 * @return The `CreditorIdentifierEntity` instance corresponding to the provided creditor ID.
 * @throws BankAccountsException.NoSuchCreditorId If the provided creditor ID does not exist in the database.
 */
fun Transaction.validatedCreditor(creditorId: UUID): CreditorIdentifierEntity =
    CreditorIdentifierEntity.find { CreditorIdentifiers.id eq creditorId }.firstOrNull()
        ?:throw BankAccountsException.NoSuchCreditorId(creditorId.toString())

/**
 * Generates a unique mandate reference for a given creditor identifier.
 *
 * This method constructs a mandate reference string in the format "MANDAT-<year>-<counter>",
 * where `<year>` denotes the current year and `<counter>` is a zero-padded incremental number
 * computed based on the last generated mandate reference for the creditor in the current year.
 *
 * @param creditorId The unique identifier of the creditor for whom the mandate is generated.
 * @param padStart The minimum length of the counter part in the mandate reference.
 *                 Defaults to 3, with zeros padded at the beginning if necessary.
 * @return A string representing the new unique mandate reference in the format "MANDAT-<year>-<counter>".
 */
fun Transaction.generateMandateReference(
    creditorId: UUID,
    padStart: Int = 3,
    mandateReferencePrefix: String = "MANDAT"
): String {
    val year = LocalDate.now().year

    // Find the last mandate for this creditor in the current year
    val lastCounter = SepaMandateEntity.find { SepaMandates.creditorIdentifierId eq creditorId }
        .mapNotNull { mandate ->
            val parts = mandate.mandateReference.split("-")
            if (parts.size == 3 && parts[1].toIntOrNull() == year) parts[2].toIntOrNull()
            else null
        }
        .maxOrNull() ?: 0

    val nextCounter = lastCounter + 1
    return "$mandateReferencePrefix-$year-${nextCounter.toString().padStart(padStart, '0')}"
}

fun Transaction.readSepaMandatesByCreditorsLegalEntity(legalEntityId: UUID): ApiSepaMandates {

    val creditorIdentifiers = CreditorIdentifierEntity.find{
        CreditorIdentifiersTable.legalEntityId eq legalEntityId
    }.toList()
    val creditorIdentifierIds = creditorIdentifiers.map{it.id.value}
    val mandates = SepaMandateEntity.find {
        SepaMandatesTable.creditorIdentifierId inList creditorIdentifierIds
    }

    return ApiSepaMandates(mandates.map { mandate ->
        mandate.toApiType()
    })
}

@Suppress("CyclomaticComplexMethod", "NoNameShadowing")
fun Transaction.updateSepaMandate(
    modifierId: UUID,
    sepaMandateId: UUID,
    creditorId: UUID,
    debtorBankAccountId: UUID,
    debtorName: String,
    signedAt: DateTime,
    validFrom: DateTime,
    validUntil: DateTime?,
    lastUsedAt: DateTime?,
    mandateReference: MandateReference? = null,
    status: MandateStatus = MandateStatus.ACTIVE,
): SepaMandateEntity {
    val sepaMandate = validatedSepaMandate(sepaMandateId)
    val hasPayments = !sepaMandate.payments.empty()
    if(hasPayments) throw SepaException.CannotUpdateSepaMandate(
        id = sepaMandateId.toString(),
        reason = "Sepa Mandate has associated payments; Needs to be amended"
    )

    val creditor = validatedCreditor(creditorId)
    val debtorBankAccount = validatedBankAccount(debtorBankAccountId)
    val mandateReference = mandateReference?.value
    // val mandateReferencePrefix = mandateReferencePrefix?.value

    val creditorChanged = creditor != sepaMandate.creditorIdentifier
    val debtorBackAccountChanged = debtorBankAccount != sepaMandate.debtorBankAccount
    val mandateReferenceChanged = mandateReference != sepaMandate.mandateReference
    val debtorNameChanged = debtorName != sepaMandate.debtorName
    val signedAtChanged = signedAt != sepaMandate.signedAt
    val validFromChanged = validFrom != sepaMandate.validFrom
    val validUntilChanged = validUntil != sepaMandate.validUntil
    val lastUsedAtChanged = lastUsedAt != sepaMandate.lastUsedAt
    val statusChanged = status != sepaMandate.status

    if(creditorChanged) sepaMandate.creditorIdentifier = creditor
    if(debtorBackAccountChanged) sepaMandate.debtorBankAccount = debtorBankAccount
    if(mandateReferenceChanged) sepaMandate.mandateReference = mandateReference.orEmpty()
    if(debtorNameChanged) sepaMandate.debtorName = debtorName
    if(signedAtChanged) sepaMandate.signedAt = signedAt
    if(validFromChanged) sepaMandate.validFrom = validFrom
    if(validUntilChanged) sepaMandate.validUntil = validUntil
    if(lastUsedAtChanged) sepaMandate.lastUsedAt = lastUsedAt
    if(statusChanged) sepaMandate.status = status

    val changed = creditorChanged
            || debtorBackAccountChanged
            || mandateReferenceChanged
            || debtorNameChanged
            || signedAtChanged
            || validFromChanged
            || statusChanged
            || lastUsedAtChanged

    if(changed) {
        sepaMandate.modifiedBy = modifierId
        sepaMandate.modifiedAt = DateTime.now()
    }

    return sepaMandate
}

fun Transaction.validatedSepaMandate(id: UUID): SepaMandateEntity = SepaMandateEntity.findById(id)
    ?: throw SepaException.NoSuchSepaMandate(id.toString())
