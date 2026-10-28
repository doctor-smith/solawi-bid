package org.solyton.solawi.bid.test.module.shares

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.workflow.admin.CreateTestUserInput
import org.solyton.solawi.bid.workflow.admin.CreateTestUserWorkflow
import org.solyton.solawi.bid.workflow.banking.FiscalYearPeriod
import org.solyton.solawi.bid.workflow.shares.*

/**
 * End-to-end coverage for the Share Management module, workflow B4:
 *
 *   PendingActivation -> (PROVIDER, SUBSCRIPTION_APPROVED) -> ClearedForAuction
 *                     -> (PROVIDER, SUBSCRIPTION_APPROVED) -> Subscribed
 */
class AuctionClearanceWorkflowTest {

    companion object {
        const val STORAGE_PATH = "AuctionClearanceWorkflowTest"

        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
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
    fun `provider clears pending subscription for auction and approves it to subscribed`() {
        val reportDirectory = "AuctionClearanceWorkflowTest"
        val suffix = System.currentTimeMillis()

        val adminUsername = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
        val adminPassword = requireNotNull(System.getenv("TEST_USER_PASSWORD")) { "TEST_USER_PASSWORD must be set" }
        val adminUser = AdminTestUser(Username(adminUsername), Password(adminPassword), STORAGE_PATH)

        // 1. Create a test user via workflow
        val testUser = withUser(adminUser on browser) {
            run(CreateTestUserWorkflow(this)) withInput
                    CreateTestUserInput(
                        Username("e2e-user@solyton.org"),
                        Password(generateSecurePassword()),
                        true,
                        "$suffix",
                        "$reportDirectory/admin-actions"
                    )
        }

        // 2. Perform workflow as the isolated test user
        withUser(testUser on browser) {
            withFailureDiagnostics("$reportDirectory/user-actions") {
                val organizationName = "e2e-shares-org-b4-$suffix"
                val shareTypeName = "e2e-share-type-b4-$suffix"
                val shareTypeKey = "key-b4-$suffix"
                val distributionPointName = "e2e-dp-b4-$suffix"
                val fiscalYearKey = "3028"
                val price = "100"

                // Setup entire share environment
                run(SetupShareEnvironmentWorkflow(this)) withInput SetupShareEnvironmentInput(
                    organizationName = organizationName,
                    fiscalYears = listOf(FiscalYearPeriod("$fiscalYearKey-01-01", "$fiscalYearKey-12-31")),
                    distributionPointName = distributionPointName,
                    shareTypeName = shareTypeName,
                    shareTypeKey = shareTypeKey,
                    shareTypeDescription = "E2E fixture share type (B4)",
                    offers = listOf(
                        ShareOfferConfig(
                            fiscalYearKey = fiscalYearKey,
                            price = price,
                            ahcAuthorizationRequired = false
                        )
                    )
                )

                // Create Share Subscription (starts PENDING_ACTIVATION)
                val subscription = run(CreateShareSubscriptionWorkflow(this)) withInput CreateShareSubscriptionInput(
                    memberName = "Jane Doe",
                    fiscalYearKey = fiscalYearKey,
                    shareTypeName = shareTypeName,
                    distributionPointName = distributionPointName,
                    price = price,
                    quantity = "1",
                    ahcAuthorized = true
                )

                // 1. Hop: PendingActivation -> CLEARED_FOR_AUCTION
                val clearedList = run(BulkChangeSubscriptionStatusWorkflow(this)) withInput BulkChangeSubscriptionStatusInput(
                    filterStatus = "All Statuses",
                    newStatus = "CLEARED_FOR_AUCTION"
                )
                val clearedRow = clearedList.item(subscription.id).getByText("CLEARED_FOR_AUCTION")
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(clearedRow).isVisible()

                // 2. Hop: ClearedForAuction -> SUBSCRIBED
                val subscribedList = run(BulkChangeSubscriptionStatusWorkflow(this)) withInput BulkChangeSubscriptionStatusInput(
                    filterStatus = "All Statuses",
                    newStatus = "SUBSCRIBED"
                )
                val subscribedRow = subscribedList.item(subscription.id).getByText("SUBSCRIBED")
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(subscribedRow).isVisible()
            }
        }
    }
}