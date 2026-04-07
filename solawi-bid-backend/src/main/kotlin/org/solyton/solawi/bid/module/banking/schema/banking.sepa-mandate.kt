package org.solyton.solawi.bid.module.banking.schema

import org.evoleq.exposedx.migrations.MigrationTable.optReference
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias SepaMandatesTable = SepaMandates
typealias SepaMandateEntity = SepaMandate

object SepaMandates : AuditableUUIDTable("sepa_mandates") {

    // -----------------------------
    // Creditor (your company)
    // -----------------------------
    val creditorIdentifierId = reference(
        "creditor_identifier_id",
        CreditorIdentifiers
    ) // SEPA Creditor Identifier (identifies your company)

    // -----------------------------
    // Debtor / Customer
    // -----------------------------
    val debtorBankAccountId = reference(
        "debtor_bank_account_id",
        BankAccounts
    ) // Customer's bank account (IBAN/BIC + account holder)

    val debtorName = varchar(
        "debtor_name",
        255
    ) // Snapshot of the customer name at the time of mandate signing

    // -----------------------------
    // Mandate identification
    // -----------------------------
    val mandateReference = varchar(
        "mandate_reference",
        64
    ) // Uniqueness handled in init block!!

    // -----------------------------
    // Signature & status
    // -----------------------------
    val signedAt = datetime(
        "signed_at"
    ) // DtOfSgntr: date when the customer signed the mandate

    val status = enumerationByName(
        "status",
        20,
        MandateStatus::class
    ).default(MandateStatus.ACTIVE) // ACTIVE, REVOKED, EXPIRED, SUSPENDED

    // -----------------------------
    // Validity period
    // -----------------------------
    val validFrom = datetime(
        "valid_from"
    ).default(DateTime.now()) // Start of validity

    val validUntil = datetime(
        "valid_until"
    ).nullable() // Optional end of validity

    // -----------------------------
    // Audit / usage tracking
    // -----------------------------
    val lastUsedAt = datetime(
        "last_used_at"
    ).nullable() // Date when mandate was last used

    val isActive = bool(
        "is_active"
    ).default(true) // Quick active flag for filtering

    // -----------------------------
    // History / amendments
    // -----------------------------
    val amendmentOf = optReference(
        "amendment_of",
        SepaMandates
    ) // Optional: points to previous mandate if amended (e.g., IBAN change)

    val collectionId = optReference("collection_id", SepaCollections)

    init {
        // unique constraint on (creditorIdentifierId + mandateReference)
        uniqueIndex("uq_creditor_mandate", creditorIdentifierId, mandateReference)
    }
}

class SepaMandate(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    var creditorIdentifier by CreditorIdentifier referencedOn SepaMandates.creditorIdentifierId
    var debtorBankAccount by BankAccount referencedOn SepaMandates.debtorBankAccountId
    var debtorName by SepaMandates.debtorName
    var mandateReference by SepaMandates.mandateReference
    var signedAt by SepaMandates.signedAt
    var status by SepaMandates.status
    var validFrom by SepaMandates.validFrom
    var validUntil by SepaMandates.validUntil
    var lastUsedAt by SepaMandates.lastUsedAt
    var isActive by SepaMandates.isActive
    var amendmentOf by SepaMandate optionalReferencedOn SepaMandates.amendmentOf
    var collection by SepaCollection optionalReferencedOn SepaMandates.collectionId

    val payments by SepaPayment referrersOn  SepaPaymentsTable.mandateId

    override var createdAt: DateTime by SepaMandates.createdAt
    override var createdBy: UUID by SepaMandates.createdBy
    override var modifiedAt: DateTime? by SepaMandates.modifiedAt
    override var modifiedBy: UUID? by SepaMandates.modifiedBy

    companion object : UUIDEntityClass<SepaMandate>(SepaMandates)


}

enum class MandateStatus {
    ACTIVE,        // valid and usable
    REVOKED,       // revoked by the customer (must not be used anymore)
    EXPIRED,       // expired (not used for 36 months)
    SUSPENDED      // temporarily disabled (can be reactivated)
}
