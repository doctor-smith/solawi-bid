package org.solyton.solawi.bid.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** Does not require Docker or a browser. */
class E2eReportTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun writesBusinessReadableRecordsAndAggregatesIndependentFailures() {
        val report = E2eReport("Approving a subscription")
        report.step("Create a member", 125)
        report.verification("Status is subscribed", 3)
        report.verification("Confirmation is visible", 7, AssertionError("secret-request-body"))
        report.verification("Expected invoice is present", 5, IllegalStateException("secret-access-token"))

        val file = tempDir.resolve("scenario.md")
        report.writeTo(file)
        val text = Files.readString(file)

        assertEquals(2, report.failedChecks())
        assertTrue(text.contains("Create a member"))
        assertTrue(text.contains("Status is subscribed"))
        assertTrue(text.contains("Confirmation is visible"))
        assertTrue(text.contains("Expected invoice is present"))
        assertTrue(text.contains("**Checks failed:** 2"))
        assertTrue(text.contains("FAIL (AssertionError)"))
        assertFalse(text.contains("secret-request-body"))
        assertFalse(text.contains("secret-access-token"))
    }

    @Test
    fun reportsSuccessWhenAllIndependentChecksPass() {
        val report = E2eReport("Creating an auction")
        report.step("Create auction", 10)
        report.verification("Auction is visible", 2)

        report.writeTo(tempDir.resolve("auction.md"))

        assertEquals(0, report.failedChecks())
        assertTrue(Files.readString(tempDir.resolve("auction.md")).contains("**Checks failed:** 0"))
    }
}
