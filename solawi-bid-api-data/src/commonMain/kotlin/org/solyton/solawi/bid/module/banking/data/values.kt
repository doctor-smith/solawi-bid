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
    // todo:dev add validation
}

@Serializable@Value
@JvmInline
value class BIC(val value: String) {
    // todo:dev add validation
}
