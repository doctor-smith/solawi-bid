
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
 * End-to-end coverage for the Share Management module, workflow D1 from
 * `.junie/progress/share-management-e2e-progress.md`:
 *
 *   Subscribed (FY1) -> (PROVIDER, NEW_PERIOD) -> RollingOver (FY1)
 *                    + Shadow Subscription in PendingActivation (FY2)
 */
class RolloverWorkflowTest {

    companion object {
        const val STORAGE_PATH = "RolloverWorkflowTest"

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
    fun `provider rolls over active subscription creating shadow subscription for next fiscal year`() {
        val reportDirectory = "RolloverWorkflowTest"
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
        withUser<Unit>(testUser on browser) {
            withFailureDiagnostics("$reportDirectory/user-actions") {
                val organizationName = "e2e-shares-org-d1-$suffix"
                val shareTypeName = "e2e-share-type-d1-$suffix"
                val shareTypeKey = "key-d1-$suffix"
                val distributionPointName = "e2e-dp-d1-$suffix"
                val fiscalYearKey1 = "3028"
                val fiscalYearKey2 = "3029"
                val price = "100"

                // Setup entire share environment
                run(SetupShareEnvironmentWorkflow(this)) withInput SetupShareEnvironmentInput(
                    organizationName = organizationName,
                    fiscalYears = listOf(
                        FiscalYearPeriod("$fiscalYearKey1-01-01", "$fiscalYearKey1-12-31"),
                        FiscalYearPeriod("$fiscalYearKey2-01-01", "$fiscalYearKey2-12-31")
                    ),
                    distributionPointName = distributionPointName,
                    shareTypeName = shareTypeName,
                    shareTypeKey = shareTypeKey,
                    shareTypeDescription = "E2E fixture share type (D1)",
                    offers = listOf(
                        ShareOfferConfig(
                            fiscalYearKey = fiscalYearKey1,
                            price = price,
                            ahcAuthorizationRequired = false,
                            sepaCollectionConfig = SepaCollectionConfig(
                                collectionKey = "D1-FY1-$suffix",
                                mandateReferencePrefix = "D1A",
                                remittanceInformation = "E2E fixture collection FY1"
                            )
                        ),
                        ShareOfferConfig(
                            fiscalYearKey = fiscalYearKey2,
                            price = price,
                            ahcAuthorizationRequired = false,
                            sepaCollectionConfig = SepaCollectionConfig(
                                collectionKey = "D1-FY2-$suffix",
                                mandateReferencePrefix = "D1A",
                                remittanceInformation = "E2E fixture collection FY2"
                            )
                        )
                    )
                )

                // Create Share Subscription in FY1
                run(CreateShareSubscriptionWorkflow(this)) withInput CreateShareSubscriptionInput(
                    memberName = "Jane Doe",
                    fiscalYearKey = fiscalYearKey1,
                    shareTypeName = shareTypeName,
                    distributionPointName = distributionPointName,
                    price = price,
                    quantity = "1",
                    ahcAuthorized = false
                )

                // Approve subscription -> transition from PENDING_ACTIVATION to SUBSCRIBED
                run(BulkChangeSubscriptionStatusWorkflow(this)) withInput BulkChangeSubscriptionStatusInput(
                    filterStatus = "All Statuses",
                    filterAhcAuth = "All Authorized",
                    newStatus = "SUBSCRIBED"
                )

                // Perform Rollover to next season (FY2 Offer)
                val targetOfferOptionText = "$fiscalYearKey2 - $shareTypeName"
                val shareSubscriptionList = run(BulkRolloverSubscriptionsWorkflow(this)) withInput BulkRolloverSubscriptionsInput(
                    filterStatus = "All Statuses",
                    filterAhcAuth = "All Authorized",
                    targetOfferOptionText = targetOfferOptionText
                )

                // Assertions:
                // 1. Original subscription in FY1 must be in status ROLLING_OVER
                val rollingOverRow = shareSubscriptionList.locator.getByText("ROLLING_OVER")
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(rollingOverRow).isVisible()

                // 2. Newly created shadow subscription in FY2 must be in status PENDING_ACTIVATION
                val pendingRow = shareSubscriptionList.locator.getByText("PENDING_ACTIVATION")
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(pendingRow).isVisible()
            }
        }
    }
}
