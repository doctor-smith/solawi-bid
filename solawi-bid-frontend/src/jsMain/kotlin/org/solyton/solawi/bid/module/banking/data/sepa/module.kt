package org.solyton.solawi.bid.module.banking.data.sepa

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.banking.data.sepa.message.SepaMessage
import org.solyton.solawi.bid.module.banking.data.sepa.message.SepaMessageString

@Lensify
data class SepaModule(
    @ReadWrite val sepaCollections: List<SepaCollection> = emptyList(),
    @ReadWrite val sepaMessageString: SepaMessageString = SepaMessageString(),
    @ReadWrite val sepaMessages: List<SepaMessage> = emptyList(),
    @ReadWrite val sepaMandates: List<SepaMandate> = emptyList()
)
