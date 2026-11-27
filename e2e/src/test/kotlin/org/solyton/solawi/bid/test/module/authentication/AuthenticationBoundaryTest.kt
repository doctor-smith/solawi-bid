package org.solyton.solawi.bid.test.module.authentication

import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Regression
import org.solyton.solawi.bid.Security
import org.solyton.solawi.bid.TestConfig
import kotlin.test.assertEquals

class AuthenticationBoundaryTest {
    @Security
    @Regression
    @Test
    fun personalMandatesRequireAuthentication() {
        Playwright.create().use { playwright ->
            val requests = playwright.request().newContext()
            try {
                val response = requests.get("${TestConfig.BACKEND_URL}/banking/sepa/mandates/personal/all")
                assertEquals(401, response.status())
            } finally {
                requests.dispose()
            }
        }
    }
}
