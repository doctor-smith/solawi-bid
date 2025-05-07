package org.solyton.solawi.bid.module.banking.service
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import org.solyton.solawi.bid.Unit

class BicValidatorTest {

    companion object {
        @JvmStatic
        fun provideBics() = listOf(
            Arguments.of("DEUTDEFF", true),   // Valid BIC for Deutsche Bank, Germany
            Arguments.of("NWBKGB2L", true),  // Valid BIC for NatWest Bank, UK
            Arguments.of("BNPAFRPP", true),  // Valid BIC for BNP Paribas, France
            Arguments.of("CHASUS33", true),  // Valid BIC for JP Morgan Chase, USA
            Arguments.of("INVALIDBIC1", false), // Invalid BIC
            Arguments.of("DEUTDEFFXXX", true),  // Valid BIC with branch code
            Arguments.of("1234DEFF", false),    // Invalid BIC (bank code contains numbers)
            Arguments.of("DEUTXXFF", false),    // Invalid country code
            Arguments.of("DEUTDEFF12@", false),  // Invalid characters in location code
            Arguments.of("SOLADEST200", true), // Valid german bic

            // More Valid BICs
            Arguments.of("DEUTDEFF", true),    // Deutsche Bank, Germany
            Arguments.of("COBADEFF", true),    // Commerzbank, Germany
            Arguments.of("NWBKGB2L", true),    // NatWest Bank, UK
            Arguments.of("BNPAFRPP", true),    // BNP Paribas, France
            Arguments.of("CHASUS33", true),    // JP Morgan Chase, USA
            Arguments.of("UBSWCHZH", true),    // UBS Bank, Switzerland
            Arguments.of("RABONL2U", true),    // Rabobank, Netherlands
            Arguments.of("BBVAESMM", true),    // BBVA, Spain
            Arguments.of("NATAAU33", true),    // National Australia Bank
            Arguments.of("TDOMCATTTOR", true), // Toronto-Dominion Bank, Canada
            Arguments.of("SOLADEST600", true), // Landesbank Baden-Württemberg, Germany

            // More Invalid BICs
            Arguments.of("INVALIDBIC1", false), // Invalid format
            Arguments.of("1234DEFF", false),    // Invalid bank code (contains numbers)
            Arguments.of("DEUTXXFF", false),    // Invalid country code
            Arguments.of("DEUTDEFF12@", false), // Invalid characters in location code
            Arguments.of("DEUTDE", false),      // Too short
            Arguments.of("DEUTDEFF1234", false) // Too long
        )
    }

    @Unit@ParameterizedTest
    @MethodSource("provideBics")
    fun `test BIC validity`(bic: String, expected: Boolean) {
        assertEquals(expected, isValidBic(bic), "BIC validation failed for: $bic")
    }
}
