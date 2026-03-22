package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.date
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias SepaMessagesTable = SepaMessages
typealias SepaMessageEntity = SepaMessage

object SepaMessages : AuditableUUIDTable("sepa_messages") {

    // -----------------------------
    // Creditor / your company
    // -----------------------------
    val creditorIdentifierId = reference(
        "creditor_identifier_id",
        CreditorIdentifiersTable
    ) // SEPA Creditor Identifier identifying the company

    val creditorAccountId = reference(
        "creditor_account_id",
        BankAccounts
    ) // Creditor's bank account from which payments are executed

    // -----------------------------
    // Message identification
    // -----------------------------
    val messageId = varchar(
        "message_id",
        64
    ).uniqueIndex() // Unique message ID, corresponds to <MsgId> in pain.008/pain.001

    val executionDate = date(
        "execution_date"
    ) // Date when the payments in this batch should be executed

    // -----------------------------
    // Status of the message
    // -----------------------------
    val status = enumerationByName(
        "status",
        20,
        SepaMessageStatus::class
    ).default(SepaMessageStatus.CREATED) // CREATED, SENT, CONFIRMED, FAILED

    // -----------------------------
    // Optional audit / summary info
    // -----------------------------
    val numberOfPayments = integer(
        "number_of_payments"
    ).default(0) // Number of transactions included in this message

    val totalAmount = double(
        "total_amount"
    ).nullable() // Sum of all payments, optional for reporting
}

class SepaMessage(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<SepaMessage>(SepaMessages)
    var creditorIdentifier by CreditorIdentifier referencedOn SepaMessages.creditorIdentifierId
    var creditorAccount by BankAccount referencedOn SepaMessages.creditorAccountId
    var messageId by SepaMessages.messageId
    var executionDate by SepaMessages.executionDate
    var status by SepaMessages.status
    var numberOfPayments by SepaMessages.numberOfPayments
    var totalAmount by SepaMessages.totalAmount

    override var createdAt: org.joda.time.DateTime by SepaMessages.createdAt
    override var createdBy: UUID by SepaMessages.createdBy
    override var modifiedAt: org.joda.time.DateTime? by SepaMessages.modifiedAt
    override var modifiedBy: UUID? by SepaMessages.modifiedBy
}


// -----------------------------
// SEPA Message status
// -----------------------------
enum class SepaMessageStatus {
    CREATED,   // Message prepared but not yet sent
    SENT,      // Message sent to bank
    CONFIRMED, // Bank confirmed execution
    FAILED     // Message rejected or failed
}
