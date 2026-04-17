package org.solyton.solawi.bid.module.banking.data.sepa.message

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.download.Download

@Lensify
data class SepaMessageString(
    // @ReadOnly val messageId: SepaMessageId,
    @ReadWrite val version: SepaMessageVersion = SepaMessageVersion.PAIN008,
    @ReadWrite val message: String = "",
    @ReadWrite val download: Download = Download.None
)

sealed class SepaMessageVersion(open val version: String) {
    data object PAIN008 : SepaMessageVersion("PAIN.008.001.02")
}
