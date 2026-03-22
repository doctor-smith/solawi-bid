package org.solyton.solawi.bid.module.shares.data.types

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite

@Lensify
data class ShareType (
    @ReadOnly val shareTypeId: String,
    @ReadWrite val providerId: String,
    @ReadWrite val name: String,
    @ReadWrite val key: String,
    @ReadWrite val description: String,
) {
    companion object {
        val default = ShareType("", "", "", "", "")
    }
}
