package org.solyton.solawi.bid.module.banking.data.sepa.message

import org.evoleq.axioms.definition.Lensify
import org.solyton.solawi.bid.module.banking.data.RemittanceInformation
import org.solyton.solawi.bid.module.banking.data.SepaMessageId

@Lensify
data class SepaMessage(
    val sepaMessageId: SepaMessageId,
    val messageIdentifier: String,
    val remittanceInformation: RemittanceInformation,
)

enum class SepaMessageStatus {
    CREATED,    // Message prepared but not yet sent
    SENT,       // Message sent to bank
    PENDING,    // Message accepted/submitted, but execution not yet confirmed
    CONFIRMED,  // Bank confirmed successful execution
    SETTLED,    // Payment has settled and funds are considered finally available
    FAILED      // Message or payment execution failed/rejected
}
