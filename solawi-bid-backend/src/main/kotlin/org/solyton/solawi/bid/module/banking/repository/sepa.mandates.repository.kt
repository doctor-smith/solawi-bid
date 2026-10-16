package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.solyton.solawi.bid.module.banking.data.MandateReference
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaMandates
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaMandateReferenceData
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.banking.service.validatedBankAccount
import java.util.*

/**
 * Creates a SEPA mandate entity with a retry mechanism to handle unique constraint violations
 * during the generation of the mandate reference. The method attempts to insert a new
 * SEPA mandate into the database up to the specified maximum number of retries.
 * If the mandate reference is already in use, the method will retry with a new unique reference.
 * If the maximum number of retries is reached without finding a unique reference, an exception is thrown.
 * If the mandate is successfully inserted, the method returns the newly created SEPA mandate entity.
 * If the mandate already exists, the method returns the existing mandate.
 * If the database throws an exception other than a unique constraint violation, the exception is re-thrown.
 *
 * Each user should have at most one active SEPA mandate per debtor bank account and mandate reference prefix.
 *
 * @param creditorIdentifierId The unique identifier of the creditor for whom the mandate is created.
 * @param debtorBankAccountId The unique identifier of the debtor's bank account associated with the mandate.
 * @param debtorName The name of the debtor associated with the bank account.
 * @param signedAt The date and time when the SEPA mandate was signed.
 * @param maxRetries The maximum number of attempts to generate a unique mandate reference. Defaults to 5.
 * @param reUseActiveMandate If true, the method will attempt to reuse an active mandate with the same mandate reference prefix.
 * @return The newly created SEPA mandate entity.
 * @throws ExposedSQLException If a database error occurs that is not a unique constraint violation.
 * @throws IllegalStateException If unable to generate a unique mandate reference after the specified number of retries.
 */
@Suppress("NoNameShadowing", "CognitiveComplexMethod")
fun Transaction.createSepaMandateWithRetry(
    creatorId: UUID,
    creditorIdentifierId: UUID,
    debtorBankAccountId: UUID,
    debtorName: String,
    signedAt: DateTime,
    validFrom: DateTime,
    validUntil: DateTime?,
    mandateReference: MandateReference? = null,
    mandateReferencePrefix: String? = null,
    status: MandateStatus = MandateStatus.ACTIVE,
    collectionId: UUID? = null,
    referenceData: CreateSepaMandateReferenceData? = null,
    maxRetries: Int = 5,
    reUseActiveMandate: Boolean = true
): SepaMandateEntity {
    val creditor = validatedCreditor(creditorIdentifierId)
    val debtorBankAccount = validatedBankAccount(debtorBankAccountId)

    repeat(maxRetries) {
        val mandateReference = mandateReference?.value ?: when(mandateReferencePrefix) {
            null -> generateMandateReference(creditorIdentifierId)
            else -> generateMandateReference(creditorIdentifierId, 3, mandateReferencePrefix)
        }
        val collection = if (collectionId != null) {
            validatedSepaCollection(collectionId)
        } else null

        val reUsedMandate = when {
            reUseActiveMandate && mandateReferencePrefix != null -> {
                val activeMandate = SepaMandateEntity.find {
                    SepaMandatesTable.mandateReference like "${mandateReferencePrefix}%" and
                    (SepaMandatesTable.status eq MandateStatus.ACTIVE) and
                    (SepaMandatesTable.debtorBankAccountId eq debtorBankAccountId)
                }.firstOrNull()
                activeMandate
            }
            else -> null
        }

        val createNewMandate = !reUseActiveMandate || reUsedMandate == null

        try {
            val sepaMandate = when (createNewMandate) {
                true -> SepaMandateEntity.new {
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
                }
                false -> reUsedMandate
            }
            // Add the collection to the mandate
            if (collectionId != null)  {
                val collection = validatedSepaCollection(collectionId)
                if (!collection.sepaMandates.contains(sepaMandate)) {
                    SepaMandateCollectionEntity.new {
                        this.sepaMandate = sepaMandate
                        this.sepaCollection = collection
                    }
                }

                syncSepaPaymentTemplate(
                    modifierId = creatorId,
                    mandate = sepaMandate,
                    collection = collection,
                )
            }

            if (referenceData != null) {
                SepaMandateDataMapping.new {
                    this.createdBy = creatorId
                    this.mandate = sepaMandate
                    this.referenceId = UUID.fromString(referenceData.referenceId.value)
                    this.amount = referenceData.amount
                    this.collection = collection
                }
                if (collection != null) syncSepaPaymentTemplate(
                    creatorId,
                    sepaMandate,
                    collection,
                )
            }

            return sepaMandate
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
 * Attaches a SEPA mandate to a reference and optionally associates it with a SEPA collection.
 * This establishes a mapping between the mandate and a specific reference. Existing mappings
 * for the same reference are intentionally kept: a reference may accumulate multiple mandate
 * mappings over time (e.g. when a mistake is corrected). Removing a mapping is an explicit
 * separate operation (detach). If a collection is provided, the association is extended to
 * the collection and the relevant payment template is synchronized.
 *
 * @param modifierId The identifier of the user or system that is making the modification.
 * @param sepaMandateId The unique ID of the SEPA mandate to be attached.
 * @param mandateReferenceId The unique reference ID associated with the mandate.
 * @param amount The payment amount to be associated with the reference.
 * @param collectionId The optional unique ID of the SEPA collection to associate with the mandate.
 * @return The SEPA mandate entity that has been linked to the reference.
 */
fun Transaction.attachSepaMandate(
    modifierId: UUID,
    sepaMandateId: UUID,
    mandateReferenceId: UUID,
    amount: Double,
    collectionId: UUID? = null,
) : SepaMandateEntity {
    val sepaMandate = validatedSepaMandate(sepaMandateId)
    val collection = collectionId?.let { validatedSepaCollection(it) }

    // Existing mappings for the same referenceId are intentionally preserved.
    // Removing a mapping requires an explicit detach operation.

    // Create new mapping entry
    SepaMandateDataMapping.new {
        this.createdBy = modifierId
        this.mandate = sepaMandate
        this.referenceId = mandateReferenceId
        this.amount = amount
        this.collection = collection
    }

    // Link the collection to the mandate and synchronize the payment template.
    // This creates the many-to-many relationship between mandate and collection,
    // enabling the collection to track associated mandates for batch payments.
    // Payment template synchronization updates recurring payment instructions
    // with the correct mandate-collection pairing and amount, ensuring proper
    // automated SEPA direct debit processing.
    if (collection != null)  {
        if (!collection.sepaMandates.contains(sepaMandate)) {
            SepaMandateCollectionEntity.new {
                this.sepaMandate = sepaMandate
                this.sepaCollection = collection
            }
        }

        syncSepaPaymentTemplate(
            modifierId = modifierId,
            mandate = sepaMandate,
            collection = collection,
        )
    }

    return sepaMandate
}

/**
 * Validates a given creditor ID by attempting to find a corresponding `CreditorIdentifierEntity` in the database.
 * If no matching entity is found, a `BankAccountsException.NoSuchCreditorId` exception is thrown.
 *
 * @param creditorIdentifierId The unique identifier of the creditor to be validated.
 * @return The `CreditorIdentifierEntity` instance corresponding to the provided creditor ID.
 * @throws BankAccountsException.NoSuchCreditorId If the provided creditor ID does not exist in the database.
 */
fun Transaction.validatedCreditor(creditorIdentifierId: UUID): CreditorIdentifierEntity =
    CreditorIdentifierEntity.find { CreditorIdentifiers.id eq creditorIdentifierId }.firstOrNull()
        ?: throw BankAccountsException.NoSuchCreditorId(creditorIdentifierId.toString())

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

    val creditorIdentifiers = CreditorIdentifierEntity.find {
        CreditorIdentifiersTable.legalEntityId eq legalEntityId
    }.toList()
    val creditorIdentifierIds = creditorIdentifiers.map { it.id.value }
    val mandates = SepaMandateEntity.find {
        SepaMandatesTable.creditorIdentifierId inList creditorIdentifierIds
    }

    return ApiSepaMandates(mandates.map { mandate ->
        mandate.toApiType()
    })
}

fun Transaction.readSepaMandatesOfUser(userId: UUID): List<SepaMandateEntity> {
    val bankAccounts = BankAccountEntity.find {
        BankAccountsTable.userId eq userId
    }.toList()
    if (bankAccounts.isEmpty()) {
        return emptyList()
    }

    return SepaMandateEntity.find {
        SepaMandatesTable.debtorBankAccountId inList bankAccounts.map { it.id.value }
    }.toList()
}

@Suppress("CyclomaticComplexMethod", "NoNameShadowing")
fun Transaction.updateSepaMandate(
    modifierId: UUID,
    sepaMandateId: UUID,
    creditorIdentifierId: UUID,
    debtorBankAccountId: UUID,
    debtorName: String,
    signedAt: DateTime,
    validFrom: DateTime,
    validUntil: DateTime?,
    lastUsedAt: DateTime?,
    isActive: Boolean,
    mandateReference: MandateReference? = null,
    status: MandateStatus = MandateStatus.ACTIVE,
): SepaMandateEntity {
    val sepaMandate = validatedSepaMandate(sepaMandateId)
    // Need a finer-grained check here, as we don't want to
    // allow updating mandates that have payments
    //
    // val hasPayments = !sepaMandate.payments.empty()
    // if(hasPayments) throw SepaException.CannotUpdateSepaMandate(
    //     id = sepaMandateId.toString(),
    //     reason = "Sepa Mandate has associated payments; Needs to be amended"
    // )

    val creditor = validatedCreditor(creditorIdentifierId)
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
    val isActiveChanged = isActive != sepaMandate.isActive

    if (creditorChanged) sepaMandate.creditorIdentifier = creditor
    if (debtorBackAccountChanged) sepaMandate.debtorBankAccount = debtorBankAccount
    if (mandateReferenceChanged) sepaMandate.mandateReference = mandateReference.orEmpty()
    if (debtorNameChanged) sepaMandate.debtorName = debtorName
    if (signedAtChanged) sepaMandate.signedAt = signedAt
    if (validFromChanged) sepaMandate.validFrom = validFrom
    if (validUntilChanged) sepaMandate.validUntil = validUntil
    if (lastUsedAtChanged) sepaMandate.lastUsedAt = lastUsedAt
    if (statusChanged) sepaMandate.status = status
    if (isActiveChanged) sepaMandate.isActive = isActive

    val changed = creditorChanged
            || debtorBackAccountChanged
            || mandateReferenceChanged
            || debtorNameChanged
            || signedAtChanged
            || validFromChanged
            || statusChanged
            || lastUsedAtChanged
            || isActiveChanged

    if (changed) {
        sepaMandate.modifiedBy = modifierId
        sepaMandate.modifiedAt = DateTime.now()
    }

    return sepaMandate
}

fun Transaction.validatedSepaMandate(id: UUID): SepaMandateEntity = SepaMandateEntity.findById(id)
    ?: throw SepaException.NoSuchSepaMandate(id.toString())
