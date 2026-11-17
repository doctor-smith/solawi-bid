package org.solyton.solawi.bid.test.module.shares

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.shares.data.api.ShareStatus
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.pageobjects.module.shares.SharesPage
import org.solyton.solawi.bid.workflow.admin.CreateTestUserInput
import org.solyton.solawi.bid.workflow.admin.CreateTestUserWorkflow
import org.solyton.solawi.bid.workflow.banking.FiscalYearPeriod
import org.solyton.solawi.bid.workflow.shares.*

/**
 * End-to-end coverage for the recoverable branch of Share Management workflow C3:
 *
 *   Subscribed -> (PROVIDER, PAYMENT_FAILED) -> PaymentFailed
 *   PaymentFailed -> (PROVIDER, PAYMENT_RECOVERED) -> Subscribed
 */
class PaymentFailureRecoveryWorkflowTest {

    companion object {
        const val STORAGE_PATH = "PaymentFailureRecoveryWorkflowTest"

        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(TestConfig.HEADLESS)
                    .setSlowMo(TestConfig.SLOW_MO)
            )
            TestUtils().getCookie(browser, STORAGE_PATH)
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            browser.close()
            playwright.close()
            TestUtils().deleteStorageStateFile(STORAGE_PATH)
        }
    }

    @Workflow
    @Test
    fun `provider records payment failure and recovers active subscription`() {
        val reportDirectory = "PaymentFailureRecoveryWorkflowTest"
        val suffix = System.currentTimeMillis()
        val adminUser = AdminTestUser(
            Username(requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }),
            Password(requireNotNull(System.getenv("TEST_USER_PASSWORD")) { "TEST_USER_PASSWORD must be set" }),
            STORAGE_PATH
        )

        val testUser = withUser(adminUser on browser) {
            run(CreateTestUserWorkflow(this)) withInput CreateTestUserInput(
                Username("e2e-user@solyton.org"),
                Password(generateSecurePassword()),
                true,
                "$suffix",
                "$reportDirectory/admin-actions"
            )
        }

        withUser<Unit>(testUser on browser) {
            withFailureDiagnostics("$reportDirectory/user-actions") {
                dismissStrayErrorDialogIfPresent(true, "$reportDirectory/user-actions/dismissed-errors")
                val fiscalYearKey = "3029"
                val shareTypeName = "e2e-share-type-c3-$suffix"

                run(SetupShareEnvironmentWorkflow(this)) withInput SetupShareEnvironmentInput(
                    organizationName = "e2e-shares-org-c3-$suffix",
                    fiscalYears = listOf(FiscalYearPeriod("$fiscalYearKey-01-01", "$fiscalYearKey-12-31")),
                    distributionPointName = "e2e-dp-c3-$suffix",
                    shareTypeName = shareTypeName,
                    shareTypeKey = "key-c3-$suffix",
                    shareTypeDescription = "E2E fixture share type (C3)",
                    offers = listOf(
                        ShareOfferConfig(
                            fiscalYearKey = fiscalYearKey,
                            price = "100",
                            ahcAuthorizationRequired = false
                        )
                    )
                )

                val subscription = run(CreateShareSubscriptionWorkflow(this)) withInput CreateShareSubscriptionInput(
                    memberName = "Jane Doe",
                    fiscalYearKey = fiscalYearKey,
                    shareTypeName = shareTypeName,
                    distributionPointName = "e2e-dp-c3-$suffix",
                    price = "100",
                    quantity = "1",
                    ahcAuthorized = true
                )

                val subscribed = run(BulkChangeSubscriptionStatusWorkflow(this)) withInput
                    BulkChangeSubscriptionStatusInput(newStatus = "SUBSCRIBED")
                assertEquals(ShareStatus.Subscribed, subscribed.all.first { it.id == subscription.id }.status)

                val paymentFailed = run(BulkChangeSubscriptionStatusWorkflow(this)) withInput
                    BulkChangeSubscriptionStatusInput(newStatus = "PAYMENT_FAILED")
                assertEquals(ShareStatus.PaymentFailed, paymentFailed.all.first { it.id == subscription.id }.status)

                val subscriptions = SharesPage(page).ShareSubscriptionManagement().listOfSubscriptions()
                assertThat(subscriptions.itemStatus(subscription.id, "PAYMENT_FAILED")).isVisible()

                val recovered = run(BulkChangeSubscriptionStatusWorkflow(this)) withInput
                    BulkChangeSubscriptionStatusInput(newStatus = "SUBSCRIBED")
                assertEquals(ShareStatus.Subscribed, recovered.all.first { it.id == subscription.id }.status)
                assertThat(subscriptions.itemStatus(subscription.id, "SUBSCRIBED")).isVisible()
            }
        }
    }
}

