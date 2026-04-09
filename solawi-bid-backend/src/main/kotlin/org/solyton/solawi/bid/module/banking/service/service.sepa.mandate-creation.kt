package org.solyton.solawi.bid.module.banking.service

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.banking.data.MandateReference
import org.solyton.solawi.bid.module.banking.data.internal.SepaMandateCreationRequest
import org.solyton.solawi.bid.module.banking.data.internal.SepaMandateResponse
import org.solyton.solawi.bid.module.banking.repository.createSepaMandateWithRetry
import org.solyton.solawi.bid.module.banking.schema.SepaMandateEntity
import java.util.*
import kotlin.random.Random

/**
 * Service for generating and managing SEPA mandates
 */
class SepaMandateGeneratorService {

    /**
    /**
     * Creates a new SEPA mandate
     *
     * @param request The request containing the mandate data
     * @return The created SEPA mandate
    */
     */
    fun Transaction.createSepaMandate(request: SepaMandateCreationRequest, creatorId: UUID): SepaMandateResponse {
        val mandate = createSepaMandateWithRetry(
            creatorId = creatorId,
            creditorIdentifierId = request.creditorId,
            debtorBankAccountId = request.debtorBankAccountId,
            debtorName = request.debtorName,
            signedAt = request.signedAt ?: DateTime.now(),
            validFrom = request.validFrom?: DateTime.now(),
            validUntil = request.validUntil
        )

        return mapToResponse(mandate)

    }

    /**
    /**
     * Generates a unique mandate reference
     * Format: CRED-YYYYMMDD-RANDOM8
    */
     */
    fun generateMandateReference(creditorId: UUID): String {
        val dateString = DateTime.now().toString("yyyyMMdd")
        val randomPart = generateRandomString(8)
        val creditorShort = creditorId.toString().take(4).uppercase()
        
        return "MAND-$creditorShort-$dateString-$randomPart"
    }

    /**
    /**
     * Creates a mandate with custom parameters
    */
     */
    fun Transaction.createSepaMandateWithCustomReference(
        creatorId: UUID,
        creditorId: UUID,
        debtorBankAccountId: UUID,
        debtorName: String,
        //  mandateReference: String? = null,
        signedAt: DateTime? = null,
        validFrom: DateTime? = null,
        validUntil: DateTime? = null
    ): SepaMandateResponse {

        // val finalMandateReference = mandateReference ?: generateMandateReference(creditorId)
        val finalSignedAt = signedAt ?: DateTime.now()
        val finalValidFrom = validFrom ?: DateTime.now()

        val mandate = createSepaMandateWithRetry(
            creatorId = creatorId,
            creditorIdentifierId = creditorId,
            debtorBankAccountId = debtorBankAccountId,
            debtorName = debtorName,
            signedAt = finalSignedAt,
            validFrom = finalValidFrom,
            validUntil = validUntil,
        )

        // Update additional fields if necessary
        if (validUntil != null) {
            mandate.validUntil = validUntil
        }

        return mapToResponse(mandate)
    }

    /**
    /**
     * Creates an amendment mandate (e.g., for IBAN change)
    */
     */
    fun Transaction.createAmendmentMandate(
        creatorId: UUID,
        originalMandateId: UUID,
        newDebtorBankAccountId: UUID,
        newDebtorName: String? = null,
        signedAt: DateTime? = null,
        mandateReference: String? = null,
    ): SepaMandateResponse {

        val originalMandate = SepaMandateEntity.findById(originalMandateId)
            ?: throw IllegalArgumentException("Original mandate not found")

        val amendmentMandate = createSepaMandateWithRetry(
            creatorId = creatorId,
            creditorIdentifierId = originalMandate.creditorIdentifier.id.value,
            debtorBankAccountId = newDebtorBankAccountId,
            debtorName = newDebtorName ?: originalMandate.debtorName,
            signedAt = signedAt ?: DateTime.now(),
            validFrom = originalMandate.validFrom,
            validUntil = originalMandate.validUntil,
            mandateReference = mandateReference?.let { MandateReference(it) }
        )

        // Set the amendment reference
        amendmentMandate.amendmentOf = originalMandate

        // Deactivate the original mandate
        originalMandate.isActive = false
        originalMandate.status = org.solyton.solawi.bid.module.banking.schema.MandateStatus.SUSPENDED

        return mapToResponse(amendmentMandate)

    }

    /**
    /**
     * Validates a mandate for usage
    */
     */
    fun validateMandateForUsage(mandateId: UUID): Boolean {
        return transaction {
            val mandate = SepaMandateEntity.findById(mandateId) ?: return@transaction false
            
            val now = DateTime.now()

            // Check status
            if (mandate.status != org.solyton.solawi.bid.module.banking.schema.MandateStatus.ACTIVE) {
                return@transaction false
            }

            // Check activity status
            if (!mandate.isActive) {
                return@transaction false
            }

            // Check validity period
            if (now.isBefore(mandate.validFrom)) {
                return@transaction false
            }

            if (mandate.validUntil != null && now.isAfter(mandate.validUntil)) {
                return@transaction false
            }

            true
        }
    }

    /**
    /**
     * Marks a mandate as used and updates lastUsedAt
    */
     */
    fun markMandateAsUsed(mandateId: UUID) {
        transaction {
            val mandate = SepaMandateEntity.findById(mandateId)
                ?: throw IllegalArgumentException("Mandate not found")
            
            mandate.lastUsedAt = DateTime.now()
        }
    }

    /**
    /**
     * Creates a batch of mandates
    */
     */
    fun createBatchMandates(requests: List<SepaMandateCreationRequest>, creatorId: UUID): List<SepaMandateResponse> {
        return transaction {
            requests.map { request ->
                val mandate = createSepaMandateWithRetry(
                    creatorId = creatorId,
                    creditorIdentifierId = request.creditorId,
                    debtorBankAccountId = request.debtorBankAccountId,
                    debtorName = request.debtorName,
                    signedAt = request.signedAt ?: DateTime.now(),
                    validFrom = request.validFrom ?: DateTime.now(),
                    validUntil = request.validUntil,

                )
                mapToResponse(mandate)
            }
        }
    }

    private fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    private fun mapToResponse(mandate: SepaMandateEntity): SepaMandateResponse {
        return SepaMandateResponse(
            id = mandate.id.value,
            creditorId = mandate.creditorIdentifier.id.value,
            debtorBankAccountId = mandate.debtorBankAccount.id.value,
            debtorName = mandate.debtorName,
            mandateReference = mandate.mandateReference,
            signedAt = mandate.signedAt,
            status = mandate.status,
            validFrom = mandate.validFrom,
            validUntil = mandate.validUntil,
            lastUsedAt = mandate.lastUsedAt,
            isActive = mandate.isActive,
            createdAt = mandate.createdAt,
            modifiedAt = mandate.modifiedAt
        )
    }
}
