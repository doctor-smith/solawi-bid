package org.solyton.solawi.bid.module.banking.data

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import org.solyton.solawi.bid.module.values.isValidUUID
import kotlin.jvm.JvmInline


@Serializable@Value
@JvmInline
value class SepaMandateId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class SepaPaymentId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class SepaCollectionId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class SepaCollectionReferenceId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class MandateReference(val value: String) {
    init {
        require(value.length in (1..35)) { "length must lie in [1,35]" }
    }
}

@Serializable@Value
@JvmInline
value class MandateReferencePrefix(val value: String) {
    init {
        require(value.length in (1..135)) { "length must lie in [1,35]" }
    }
}

@Serializable@Value
@JvmInline
value class RemittanceInformation(val value: String) {
    init {
        require(value.length <= 140)
    }
}


@Serializable@Value
@JvmInline
value class LocalInstrument(val value: String) {
    init {
        require(value.length <= 10)
    }
}

@Serializable@Value
@JvmInline
value class ChargeBearer(val value: String) {
    init {
        require(value.length <= 4)
    }
}

@Serializable@Value
@JvmInline
value class PurposeCode(val value: String) {
    init {
        require(value.length <= 4)
    }
}
