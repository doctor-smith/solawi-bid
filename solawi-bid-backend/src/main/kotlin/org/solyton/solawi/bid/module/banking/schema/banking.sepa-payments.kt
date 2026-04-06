package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.date
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

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
}

class SepaPayment(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<SepaPayment>(SepaPayments)

    var mandate by SepaMandate referencedOn SepaPayments.mandateId
    var collection by SepaCollection referencedOn SepaPayments.collectionId

    var amount by SepaPayments.amount
    var executionDate by SepaPayments.executionDate
    var sequenceType by SepaPayments.sequenceType
    var states by SepaPayments.status
    var failureReason by SepaPayments.failureReason

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
    PENDING
}
