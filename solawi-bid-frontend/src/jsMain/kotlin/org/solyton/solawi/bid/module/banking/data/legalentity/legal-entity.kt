package org.solyton.solawi.bid.module.banking.data.legalentity

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.uuid.NIL_UUID
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.values.LegalEntityId

@Lensify
data class LegalEntity(
    @ReadOnly val legalEntityId: LegalEntityId,
    @ReadOnly val partyId: LegalEntityId,
    @ReadWrite val name: String = "",
    @ReadWrite val legalForm: String? = null,
    @ReadWrite val legalEntityType: LegalEntityType = LegalEntityType.HUMAN,
    @ReadWrite val address: Address
) {
    companion object
    {
        val default = LegalEntity(
            LegalEntityId(NIL_UUID),
            LegalEntityId(NIL_UUID),
            "",
            "",
            LegalEntityType.HUMAN,
            Address.default()
        )
    }
}

enum class LegalEntityType {
    HUMAN,
    ORGANIZATION
}
