package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.banking.data.RemittanceInformation
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.banking.service.generateE2ETransactionId
import org.solyton.solawi.bid.module.banking.service.generateMessageId
import java.util.*

fun Transaction.readSepaMessagesByLegalEntity(
    legalEntityId: UUID
): List<SepaMessageEntity> {
    // Each sepa message is associated with a creditor identifier
    // We can use this fact to find the creditor identifier for the given legal entity
    // and then retrieve the sepa messages associated with that creditor identifier
    val creditorIdentifier = CreditorIdentifierEntity.find{
        CreditorIdentifiersTable.legalEntityId eq legalEntityId
    }.firstOrNull()

    if(creditorIdentifier == null) return emptyList()

    return creditorIdentifier.sepaMessages.toList()
}

/**
 * Updates the status of a SEPA message and adjusts the payment execution statuses
 * linked to the message accordingly.
 *
 * @param modifierId The unique identifier of the user or system making the modification.
 * @param sepaMessageId The unique identifier of the SEPA message to update.
 * @param status The new status to apply to the SEPA message.
 * @return The updated SEPA message with the new status applied.
 * @throws SepaException.Message.NoSuchMessage If the SEPA message with the provided ID does not exist.
 * @throws SepaException.Message.InvalidStatusTransition If the status transition is not allowed for the SEPA message.
 */
fun Transaction.updateSepaMessageStatus(
    modifierId: UUID,
    sepaMessageId: UUID,
    status: SepaMessageStatus,
    updatePayments: Boolean = false
): SepaMessage {
    val sepaMessage = SepaMessageEntity.find{ SepaMessagesTable.id eq sepaMessageId }.firstOrNull()
        ?:throw SepaException.Message.NoSuchMessage(sepaMessageId.toString())

    if(!sepaMessage.isTransitionAllowed(status)) throw SepaException.Message.InvalidStatusTransition(sepaMessageId.toString(), status)

    if(updatePayments) {
        val newPaymentStatus = when (status) {
            SepaMessageStatus.CREATED, SepaMessageStatus.MERGED -> PaymentExecutionStatus.MESSAGE_CREATED
            SepaMessageStatus.SENT -> PaymentExecutionStatus.SENT
            SepaMessageStatus.PENDING -> PaymentExecutionStatus.SENT
            SepaMessageStatus.CONFIRMED -> PaymentExecutionStatus.PENDING
            SepaMessageStatus.FAILED -> PaymentExecutionStatus.CREATED
            SepaMessageStatus.SETTLED -> PaymentExecutionStatus.MESSAGE_SETTLED
        }
        updateSepaPaymentExecutionStatuses(
            modifier = modifierId,
            paymentIds = sepaMessage.payments.map { it.id.value },
            newStatus = newPaymentStatus,
            updateMessages = false
        )
    }

    return sepaMessage.apply{ this.status = status }
}

val sepaMessageStatusTransitions: Map<SepaMessageStatus, Set<SepaMessageStatus>> by lazy {
    mapOf(
        SepaMessageStatus.CREATED to setOf(SepaMessageStatus.SENT, SepaMessageStatus.MERGED),
        SepaMessageStatus.SENT to setOf(SepaMessageStatus.PENDING, SepaMessageStatus.FAILED),
        SepaMessageStatus.PENDING to setOf(SepaMessageStatus.CONFIRMED, SepaMessageStatus.FAILED),
        SepaMessageStatus.CONFIRMED to setOf(SepaMessageStatus.SETTLED),
        SepaMessageStatus.FAILED to setOf(SepaMessageStatus.CREATED),
        SepaMessageStatus.SETTLED to setOf(),
        SepaMessageStatus.MERGED to setOf()
    )
}

fun SepaMessageEntity.isTransitionAllowed(newStatus: SepaMessageStatus): Boolean =
    sepaMessageStatusTransitions[status]?.contains(newStatus) ?: false

/**
 * Merges multiple SEPA messages into a single SEPA message and updates the related payments and collections.
 *
 * This method validates that all SEPA messages to be merged have the status `CREATED`, and ensures that all
 * payments within those messages belong to the same collection. It then creates a new SEPA message with
 * combined payment information and updates the status and properties of the entities involved.
 *
 * @param modifierId The identifier of the user or system modifying the SEPA messages.
 * @param sepaMessageIds A list of UUIDs representing the SEPA messages to be merged.
 * @param executionDate The execution date for the merged SEPA message.
 * @param remittanceInformation Additional remittance information associated with the merged SEPA message.
 * @return The newly created merged SEPA message.
 * @throws IllegalArgumentException If not all SEPA messages have the `CREATED` status or if the payments
 * associated with the SEPA messages belong to different collections.
 */
fun Transaction.mergeSepaMessages(
    modifierId: UUID,
    sepaMessageIds: List<UUID>,
    executionDate: DateTime,
    remittanceInformation: RemittanceInformation
): List<SepaMessage> {
    val sepaMessages = SepaMessageEntity.find { SepaMessagesTable.id inList sepaMessageIds }.toList()

    require(sepaMessages.all { it.status == SepaMessageStatus.CREATED }) {
        "All sepa messages must have status ${SepaMessageStatus.CREATED.name}"
    }

    val payments = sepaMessages.flatMap { it.payments }.distinct()
    val collections = payments.map { it.collection }

    require(collections.distinctBy{ it.id.value }.size == 1) {
        "All payments must belong to the same collection"
    }
    val collection = collections.first()

    val messageId = generateMessageId()
    val totalAmount = payments.sumOf { it.amount }

    val merged = SepaMessageEntity.new {
        this.createdBy = modifierId
        this.creditorIdentifier = collection.creditorIdentifier
        this.creditorAccount = collection.creditorAccount
        this.messageId = messageId
        this.executionDate = executionDate
        this.status = SepaMessageStatus.CREATED
        this.numberOfPayments = payments.size
        this.totalAmount = totalAmount
        this.remittanceInformation = remittanceInformation.value
    }

    payments.forEach {
        it.modifiedBy = modifierId
        it.modifiedAt = DateTime.now()
        it.message = merged
        it.executionDate = executionDate
        it.endToEndId = generateE2ETransactionId()
    }

    sepaMessages.forEach {
        it.modifiedBy = modifierId
        it.modifiedAt = DateTime.now()
        it.status = SepaMessageStatus.MERGED
    }

    return sepaMessages + merged
}
