package org.solyton.solawi.bid.module.bid.data.values

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import org.solyton.solawi.bid.module.values.isValidUUID
import kotlin.jvm.JvmInline

@Serializable @Value
@JvmInline
value class AuctionId(val value: String) {
    init {
        isValidUUID( value)
    }
}
