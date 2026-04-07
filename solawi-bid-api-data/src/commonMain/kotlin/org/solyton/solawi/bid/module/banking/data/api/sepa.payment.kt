package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId

typealias ApiSepaPayment = SepaPayment
typealias ApiSepaPayments = SepaPayments
typealias ApiPaymentExecutionStatus = PaymentExecutionStatus

@Serializable
data class SepaPayments(
    val all: List<SepaPayment>
)

@Serializable
data class SepaPayment(
    val sepaPaymentId: SepaPaymentId,
    val sepaMandateId: SepaMandateId,
    val sepaCollectionId: SepaCollectionId,
    val amount: Double,
    val executionDate: LocalDate,
    val sequenceType: SepaSequenceType,
    val status: PaymentExecutionStatus,
    val failureReason: String? = null
)

@Serializable
data class CreateSepaPayment(
    val sepaMandateId: SepaMandateId,
    val sepaCollectionId: SepaCollectionId,
    val amount: Double,
    val executionDate: LocalDate,
    val sequenceType: SepaSequenceType,
    val status: PaymentExecutionStatus,
    val failureReason: String? = null
)


@Serializable
data class ReadSepaPaymentsByLegalEntity(
    /**
     * takes param
     * "legal_entity"
     */
    override val queryParams: QueryParams
) : Parameters()


@Serializable
data class UpdateSepaPayment(
    val sepaPaymentId: SepaPaymentId,
    val sepaMandateId: SepaMandateId,
    val sepaCollectionId: SepaCollectionId,
    val amount: Double,
    val executionDate: LocalDate,
    val sequenceType: SepaSequenceType,
    val status: PaymentExecutionStatus,
    val failureReason: String? = null
)



@Serializable
data class AddSepaPaymentToCollection(
    val sepaMandateId: SepaMandateId,
    val sepaCollectionId: SepaCollectionId
)

@Serializable
data class RemoveSepaPaymentFromCollection(
    val sepaMandateId: SepaMandateId,
    val sepaCollectionId: SepaCollectionId
)

@Serializable
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
