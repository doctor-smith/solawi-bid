package org.solyton.solawi.bid.module.permission.data

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import org.solyton.solawi.bid.module.values.isValidUUID
import kotlin.jvm.JvmInline

@Serializable @Value
@JvmInline
value class ContextName(val value: String)

@Serializable @Value
@JvmInline
value class RoleName(val value: String)

@Serializable @Value
@JvmInline
value class RightName(val value: String)

@Serializable @Value
@JvmInline
value class RightId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable @Value
@JvmInline
value class RoleId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable @Value
@JvmInline
value class ContextId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}
