package org.solyton.solawi.bid.module.banking.data.sepa.payment

import kotlinx.datetime.LocalDate
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.SepaSequenceType

@Lensify
data class SepaPayment(
    @ReadOnly val sepaPaymentId: SepaPaymentId,
    @ReadWrite val sepaMandateId: SepaMandateId,
    @ReadWrite val sepaCollectionId: SepaCollectionId,
    @ReadWrite val amount: Double,
    @ReadWrite val executionDate: LocalDate,
    @ReadWrite val sequenceType: SepaSequenceType,
    @ReadWrite val status: PaymentExecutionStatus,
    @ReadWrite val failureReason: String? = null
)
