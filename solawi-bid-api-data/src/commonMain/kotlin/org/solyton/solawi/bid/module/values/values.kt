package org.solyton.solawi.bid.module.values

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import kotlin.jvm.JvmInline

@Serializable@Value
@JvmInline
value class Uuid(val id: String) {
    init {
        require(isValidUUID(id)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class UserId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}


@Serializable@Value
@JvmInline
value class LegalEntityId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}


@Serializable@Value
@JvmInline
value class AccessorId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class ProviderId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class Username(val value: String) {
    init {
        require(isValidEmail(value)) { "Username must be a valid email" }
    }
}
