package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.jodatime.date
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias SepaPaymentsTable = SepaPayments
typealias SepaPaymentEntity = SepaPayment

object SepaPayments : AuditableUUIDTable("sepa_payments") {
    val mandateId = reference("mandate_id", SepaMandates)
    val collectionId = reference("collection_id", SepaCollections)
    val amount = double("amount")
    val executionDate = date("execution_date")
    val sequenceType = enumerationByName("sequence_type", 10, SepaSequenceType::class)
    val status = enumerationByName("status", 20, PaymentExecutionStatus::class)
    val failureReason = text("failure_reason").nullable()
    val endToEndId = varchar("end_to_end_id", 35).uniqueIndex().nullable()
    val templateId = optReference("template_id", SepaPaymentTemplates)
    // Deprecated, use sepa-payment-links instead
    val successorId = optReference(
        "successor_id",
        SepaPayments,
        onDelete = ReferenceOption.SET_NULL
    ).uniqueIndex()
    val messageId = optReference(
        "message_id",
        SepaMessages,
        onDelete = ReferenceOption.SET_NULL
    )
}

class SepaPayment(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<SepaPayment>(SepaPayments)

    var mandate by SepaMandate referencedOn SepaPayments.mandateId
    var collection by SepaCollection referencedOn SepaPayments.collectionId

    var amount by SepaPayments.amount
    var executionDate by SepaPayments.executionDate
    var sequenceType by SepaPayments.sequenceType
    var status by SepaPayments.status
    var failureReason by SepaPayments.failureReason
    var endToEndId by SepaPayments.endToEndId
    var template by SepaPaymentTemplate optionalReferencedOn SepaPayments.templateId
    val predecessors by SepaPayment.via(SepaPaymentLinks.successorId, SepaPaymentLinks.predecessorId)
    val successors by SepaPayment.via(SepaPaymentLinks.predecessorId, SepaPaymentLinks.successorId)

    var message by SepaMessage optionalReferencedOn SepaPayments.messageId

    val nextPeriodSuccessor: SepaPayment?
        get() = SepaPaymentLink.find {
            (SepaPaymentLinks.predecessorId eq this@SepaPayment.id) and (SepaPaymentLinks.kind eq SuccessorKind.NEXT_PERIOD)
        }.firstOrNull()?.successor

    val retrySuccessor: SepaPayment?
        get() = SepaPaymentLink.find {
            (SepaPaymentLinks.predecessorId eq this@SepaPayment.id) and (SepaPaymentLinks.kind eq SuccessorKind.RETRY)
        }.firstOrNull()?.successor

    val mergeSuccessor: SepaPayment?
        get() = SepaPaymentLink.find {
            (SepaPaymentLinks.predecessorId eq this@SepaPayment.id) and (SepaPaymentLinks.kind eq SuccessorKind.MERGE)
        }.firstOrNull()?.successor

    override var createdAt: DateTime by SepaPayments.createdAt
    override var createdBy: UUID by SepaPayments.createdBy
    override var modifiedAt: DateTime? by SepaPayments.modifiedAt
    override var modifiedBy: UUID? by SepaPayments.modifiedBy
}

// Represents the current execution status of a SEPA payment
enum class PaymentExecutionStatus {
    /**
     * CREATED: Payment has been created in the system but not yet sent to the bank.
     */
    CREATED,
    /**
     * MESSAGE_CREATED: Sepa Message has been created and PAIN.008 has been delivered to the client
      */
    MESSAGE_CREATED,
    /**
     * SENT: Payment has been submitted to the bank for processing.
     */
    SENT,

    /**
     * CONFIRMED: Bank confirmed that the payment has been successfully executed.
     */
    CONFIRMED,

    /**
     * FAILED: Bank rejected or returned the payment (e.g., insufficient funds, invalid IBAN).
     * The reason can be found in Payment.failureReason or BankStatus.
     */
    FAILED,

    /**
     * PENDING: Payment is under review by the bank or awaiting settlement.
     */
    PENDING,

    /**
     * PAYED_MANUALLY: Payment was manually processed by the client.
     */
    PAYED_MANUALLY,

    /**
     * DROPPED: Payment has been cancelled or dropped from processing and will not be executed.
     */
    DROPPED,
}
