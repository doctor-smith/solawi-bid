package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias SepaCollectionsTable = SepaCollections
typealias SepaCollectionEntity = SepaCollection

object SepaCollections : AuditableUUIDTable("sepa_collections") {

    // Reference to the SEPA creditor identifier (who is collecting the money)
    val creditorIdentifierId = reference("creditor_identifier_id", CreditorIdentifiersTable)

    // Bank account (IBAN/BIC) where the collected funds will be credited
    val creditorAccountId = reference("creditor_account_id", BankAccounts)

    // Prefix used to generate unique mandate references (must be unique per mandate)
    val mandateReferencePrefix = varchar("mandate_reference_prefix", 35)

    // Default remittance information shown to the debtor (usage / payment description)
    val remittanceInformation = varchar("remittance_information", 140)

    // Default SEPA sequence type (e.g. FRST, RCUR, OOFF, FNAL)
    val sequenceType = enumerationByName("sequence_type", 10, SepaSequenceType::class)

    // SEPA scheme / instrument (e.g. CORE or B2B)
    val localInstrument = varchar("local_instrument", 10).nullable()

    // Indicates who bears the transaction costs (usually "SLEV" in SEPA)
    val chargeBearer = varchar("charge_bearer", 4).default("SLEV")

    // Preferred day of month for collection (e.g. 1 = first day, 15 = mid-month)
    val requestedCollectionDay = integer("requested_collection_day").nullable()

    // Number of days between submission and execution (used for lead time / pre-notification)
    val leadTimeDays = integer("lead_time_days").default(2)

    // Optional SEPA purpose code (e.g. RENT, SALA)
    val purposeCode = varchar("purpose_code", 4).nullable()

    // Prefix for generating EndToEndId values (used for tracking payments)
    val endToEndIdPrefix = varchar("end_to_end_id_prefix", 35).nullable()

    // Whether failed debit attempts should be retried automatically
    val retryOnFailure = bool("retry_on_failure").default(false)

    // Maximum number of retry attempts for failed payments
    val maxRetries = integer("max_retries").default(0)

    // Indicates whether this collection configuration is active
    val isActive = bool("is_active").default(true)
}

class SepaCollection(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<SepaCollection>(SepaCollections)

    var creditorIdentifier by CreditorIdentifier referencedOn SepaCollections.creditorIdentifierId
    var creditorAccount by BankAccount referencedOn SepaCollections.creditorAccountId
    var mandateReferencePrefix by SepaCollections.mandateReferencePrefix
    var remittanceInformation by SepaCollections.remittanceInformation
    var sequenceType by SepaCollections.sequenceType
    var localInstrument by SepaCollections.localInstrument
    var chargeBearer by SepaCollections.chargeBearer
    var requestedCollectionDay by SepaCollections.requestedCollectionDay
    var leadTimesDays by SepaCollections.leadTimeDays
    var purposeCode by SepaCollections.purposeCode
    var endToEndIdPrefix by SepaCollections.endToEndIdPrefix
    var retryOnFailure by SepaCollections.retryOnFailure
    var maxRetries by SepaCollections.maxRetries

    var isActive by SepaCollections.isActive

    val sepaMandates by SepaMandate optionalReferrersOn SepaMandates.collectionId
    val sepaPayments by SepaPayment referrersOn SepaPayments.collectionId

    val referenceIds by SepaCollectionMapping referrersOn SepaCollectionMappings.sepaCollectionId

    override var createdAt: DateTime by SepaCollections.createdAt
    override var createdBy: UUID by SepaCollections.createdBy
    override var modifiedAt: DateTime? by SepaCollections.modifiedAt
    override var modifiedBy: UUID? by SepaCollections.modifiedBy
}


// SEPA Sequence Type indicates whether a debit is first, recurring, one-off, or final
enum class SepaSequenceType {
    /**
     * FRST (First): First direct debit of a new mandate.
     * Used for the initial collection after the mandate signature.
     */
    FRST,

    /**
     * RCUR (Recurring): All subsequent direct debits after the first.
     * Typical for regular, recurring payments (e.g., monthly subscription).
     */
    RCUR,

    /**
     * OOFF (One-Off): One-time direct debit that is not recurring.
     * Used for special payments outside a recurring plan.
     */
    OOFF,

    /**
     * FNAL (Final): The final direct debit in a mandate cycle.
     * Used when a mandate ends or the last payment of a series is collected.
     */
    FNAL
}
