package org.solyton.solawi.bid.module.banking.data.sepa.message

import kotlinx.datetime.LocalDate
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.RemittanceInformation
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.SepaMessageIdentifier
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId

@Lensify
data class SepaMessage(
    @ReadOnly val sepaMessageId: SepaMessageId,
    @ReadOnly val messageIdentifier: SepaMessageIdentifier,
    @ReadWrite val remittanceInformation: RemittanceInformation,
    @ReadWrite val executionDate: LocalDate,
    @ReadWrite val status: SepaMessageStatus,
    @ReadWrite val totalAmount: Double?,
    @ReadWrite val numberOfPayments: Int,
    @ReadWrite val paymentIds: List<SepaPaymentId>,
)

enum class SepaMessageStatus {
    CREATED,    // Message prepared but not yet sent
    SENT,       // Message sent to bank
    PENDING,    // Message accepted/submitted, but execution not yet confirmed
    CONFIRMED,  // Bank confirmed successful execution
    SETTLED,    // Payment has settled and funds are considered finally available
    FAILED,     // Message or payment execution failed/rejected
    MERGED
}
