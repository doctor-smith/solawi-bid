package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import org.solyton.solawi.bid.module.values.isValidUUID
import kotlin.jvm.JvmInline

@Serializable@Value
@JvmInline
value class OrganizationId(val value: String) {
    init {
        require(isValidUUID(value)) { "value of OrganizationId must be a valid UUID" }
    }
}
