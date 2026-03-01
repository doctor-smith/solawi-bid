package org.solyton.solawi.bid.module.shares.data.values

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import org.solyton.solawi.bid.module.values.isValidUUID
import kotlin.jvm.JvmInline

@Serializable @Value
@JvmInline
value class ShareSubscriptionId(val value: String) {
    init {
        isValidUUID(value)
    }
}

@Serializable @Value
@JvmInline
value class ShareOfferId(val value: String) {
    init {
        isValidUUID(value)
    }
}

@Serializable @Value
@JvmInline
value class ShareTypeId(val value: String) {
    init {
        isValidUUID(value)
    }
}
