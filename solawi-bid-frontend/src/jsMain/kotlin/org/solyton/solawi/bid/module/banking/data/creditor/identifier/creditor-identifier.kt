package org.solyton.solawi.bid.module.banking.data.creditor.identifier

import kotlinx.datetime.LocalDate
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.CreditorId
import org.solyton.solawi.bid.module.banking.data.CreditorIdentifierId
import org.solyton.solawi.bid.module.values.LegalEntityId

@Lensify
data class CreditorIdentifier(
    @ReadOnly val creditorIdentifierId: CreditorIdentifierId,
    @ReadWrite val legalEntityId: LegalEntityId,
    @ReadWrite val creditorId: CreditorId,
    @ReadWrite val validFrom: LocalDate,
    @ReadWrite val validUntil: LocalDate?,
    @ReadWrite val isActive: Boolean = true
)
