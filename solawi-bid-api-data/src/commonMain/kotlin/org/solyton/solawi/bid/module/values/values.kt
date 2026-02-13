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
value class UserId(val id: String) {
    init {
        require(isValidUUID(id)) { "Id must be a valid UUID" }
    }
}


@Serializable@Value
@JvmInline
value class LegalEntityId(val id: String) {
    init {
        require(isValidUUID(id)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class ProviderId(val id: String) {
    init {
        require(isValidUUID(id)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class Username(val username: String) {
    init {
        require(isValidEmail(username)) { "Username must be a valid email" }
    }
}
