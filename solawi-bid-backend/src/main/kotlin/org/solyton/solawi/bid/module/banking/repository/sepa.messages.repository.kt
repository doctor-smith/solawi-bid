package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.*
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
            SepaMessageStatus.CREATED -> PaymentExecutionStatus.MESSAGE_CREATED
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
        SepaMessageStatus.CREATED to setOf(SepaMessageStatus.SENT),
        SepaMessageStatus.SENT to setOf(SepaMessageStatus.PENDING, SepaMessageStatus.FAILED),
        SepaMessageStatus.PENDING to setOf(SepaMessageStatus.CONFIRMED, SepaMessageStatus.FAILED),
        SepaMessageStatus.CONFIRMED to setOf(SepaMessageStatus.SETTLED),
        SepaMessageStatus.FAILED to setOf(SepaMessageStatus.CREATED),
        SepaMessageStatus.SETTLED to setOf(),
    )
}

fun SepaMessageEntity.isTransitionAllowed(newStatus: SepaMessageStatus): Boolean =
    sepaMessageStatusTransitions[status]?.contains(newStatus) ?: false
