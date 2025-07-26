package org.solyton.solawi.bid.module.banking.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.Unit

class IbanValidatorTest {

    companion object {
        @JvmStatic
        fun provideIbans() = listOf(
            Arguments.of("DE89370400440532013000", true), // Valid German IBAN
            Arguments.of("GB82WEST12345698765432", true), // Valid UK IBAN
            Arguments.of("FR1420041010050500013M02606", true), // Valid French IBAN
            Arguments.of("INVALIDIBAN123", false),         // Invalid IBAN
            Arguments.of("CH9300762011623852957", true),  // Valid Swiss IBAN
            Arguments.of("DK5000400440116243", true),      // Valid Danish IBAN
            Arguments.of("DE52600501017890123456", true), // Valid german IBAN
            // More Valid IBANs (structured correctly, non-personal)
            Arguments.of("DE89370400440532013000", true), // Germany
            Arguments.of("GB82WEST12345698765432", true), // United Kingdom
            Arguments.of("FR1420041010050500013M02606", true), // France
            Arguments.of("CH9300762011623852957", true), // Switzerland
            Arguments.of("ES9121000418450200051332", true), // Spain
            Arguments.of("NL91ABNA0417164300", true), // Netherlands
            Arguments.of("IT60X0542811101000000123456", true), // Italy
            Arguments.of("SE3550000000054910000003", true), // Sweden
            Arguments.of("NO9386011117947", true), // Norway
            Arguments.of("FI2112345600000785", true), // Finland

            // More Invalid IBANs
            Arguments.of("DE8937040044053201300", false), // Too short
            Arguments.of("GB82WEST1234569876543X", false), // Invalid character
            Arguments.of("FR1420041010050500013M026066", false), // Too long
            Arguments.of("CH930076201162385295", false), // Too short
            Arguments.of("INVALIDIBAN123", false), // Completely invalid format
            Arguments.of("NL91ABNA04171643000", false), // Too long
            Arguments.of("DE89370400440532013000123", false), // Too long
            Arguments.of("ES912100041845020005133X", false), // Invalid character
            Arguments.of("NO938601111794", false), // Too short
            Arguments.of("FI21A2345600000785", false) // Invalid character in numeric section
        )
    }

    @Unit@ParameterizedTest
    @MethodSource("provideIbans")
    fun validateIban(iban: String, expected: Boolean) {
        assertEquals(expected, isValidIban(iban), "IBAN validation failed for: $iban")
    }
}
