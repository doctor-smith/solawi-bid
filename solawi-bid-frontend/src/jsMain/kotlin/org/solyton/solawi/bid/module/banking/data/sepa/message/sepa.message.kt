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
