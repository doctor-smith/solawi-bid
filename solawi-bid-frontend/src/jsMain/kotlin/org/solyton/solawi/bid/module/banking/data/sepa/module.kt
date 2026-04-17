package org.solyton.solawi.bid.module.banking.data.sepa

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.sepa.message.SepaMessageString
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection

@Lensify
data class SepaModule(
    @ReadWrite val sepaCollections: List<SepaCollection> = emptyList(),
    @ReadWrite val sepaMessageString: SepaMessageString = SepaMessageString()
)
