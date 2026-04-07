package org.solyton.solawi.bid.module.banking.data.sepa.mandate

import kotlinx.datetime.LocalDateTime
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.uuid.NIL_UUID
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.MandateReference
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.sepa.MandateStatus

@Lensify
data class SepaMandate(
    @ReadOnly val sepaMandateId: SepaMandateId,// = SepaMandateId(NIL_UUID),
    @ReadWrite val debtorBankAccountId: BankAccountId,
    @ReadWrite val debtorName: String,
    @ReadWrite val mandateReference: MandateReference,
    @ReadWrite val signedAt: LocalDateTime,
    @ReadWrite val validFrom: LocalDateTime,
    @ReadWrite val validUntil: LocalDateTime?,
    @ReadWrite val lastUsedAt: LocalDateTime?,
    @ReadWrite val status: MandateStatus = MandateStatus.ACTIVE,
    @ReadWrite val isActive: Boolean = true,
    @ReadWrite val amendmentOf: SepaMandateId? = null,
    @ReadWrite val collectionId: SepaCollectionId? = null
)
