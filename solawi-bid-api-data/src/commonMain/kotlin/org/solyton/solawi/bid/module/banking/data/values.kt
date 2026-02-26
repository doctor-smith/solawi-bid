package org.solyton.solawi.bid.module.banking.data

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import org.solyton.solawi.bid.module.values.isValidUUID
import kotlin.jvm.JvmInline


@Serializable@Value
@JvmInline
value class BankAccountId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class FiscalYearId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class IBAN(val value: String) {
    init {
        require(value.length in 15..34) { "IBAN length must be between 15 and 34 characters" }
        require(value.matches(Regex("^[A-Z]{2}[0-9A-Z]{13,32}$"))) { "Invalid IBAN format" }
    }
}

@Serializable@Value
@JvmInline
value class BIC(val value: String) {
    init {
        require(value.matches(Regex("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$"))) { "Invalid BIC format" }
    }
}
