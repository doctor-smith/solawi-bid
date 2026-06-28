package org.solyton.solawi.bid.module.banking.data.sepa.payment

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.sepa.SuccessorKind

@Lensify
data class SepaPaymentLink(
    @ReadOnly val source: SepaPaymentId,
    @ReadOnly val target: SepaPaymentId,
    @ReadOnly val kind: SuccessorKind
)
