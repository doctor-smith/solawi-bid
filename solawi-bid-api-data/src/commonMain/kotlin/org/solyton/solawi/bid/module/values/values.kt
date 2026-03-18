package org.solyton.solawi.bid.module.values

import kotlinx.serialization.Serializable
import org.evoleq.axioms.definition.Value
import kotlin.jvm.JvmInline
import kotlin.math.round

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
value class CreatorId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}


@Serializable@Value
@JvmInline
value class ModifierId(val value: String) {
    init {
        require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class UserProfileId(val value: String) {
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

@Serializable//@Value
@JvmInline
value class Price(val value: Double) {
    init {
        // require(isValidPrecision(value)) { "Price must be a multiple of 0.01." }
    }

    fun isValidPrecision(value: Double): Boolean = (value % 0.01) == 0.00

    fun toCentPrecision(): Double = value * 100


    fun toDouble(): Double = value

    fun format(): String {
        val rounded = round(value * 100) / 100
        val parts = rounded.toString().split(".")

        val integerPart = parts[0]
        val decimalPart = parts.getOrElse(1) { "0" }

        val paddedDecimal = decimalPart.padEnd(2, '0')

        return "$integerPart,$paddedDecimal"
    }
    companion object {
        fun fromString(value: String): Price = Price(round(value.replace(",", ".").toDouble() * 100) / 100)

        fun fromCentPrecision(value: Int): Price = Price(value.toDouble() / 100)
    }
}

@Serializable@Value
@JvmInline
value class Firstname(val value: String)

@Serializable@Value
@JvmInline
value class Lastname(val value: String)

@Serializable@Value
@JvmInline
value class Title(val value: String)




@Serializable@Value
@JvmInline
value class PhoneNumber(val value: String) {
    init {
        // require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}

@Serializable@Value
@JvmInline
value class Description(val value: String) {
    init {
        // require(isValidUUID(value)) { "Id must be a valid UUID" }
    }
}
