package org.solyton.solawi.bid.module.application.data

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import org.solyton.solawi.bid.module.values.isValidUUID
import kotlin.jvm.JvmInline

@Serializable@Value
@JvmInline
value class ApplicationId(val value: String) {
    init {
        require(isValidUUID(value)) { "value must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class ModuleId(val value: String) {
    init {
        require(isValidUUID(value)) { "value must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class ApplicationName(val value: String)


@Serializable@Value
@JvmInline
value class ModuleName(val value: String)
