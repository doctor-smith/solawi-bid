package org.solyton.solawi.bid.test.module.shares

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.pageobjects.module.shares.SharesPage
import org.solyton.solawi.bid.workflow.admin.CreateTestUserInput
import org.solyton.solawi.bid.workflow.admin.CreateTestUserWorkflow
import org.solyton.solawi.bid.workflow.banking.FiscalYearPeriod
import org.solyton.solawi.bid.workflow.shares.*

/**
 * End-to-end coverage for the Share Management module, workflow G1 from
 * `.junie/progress/share-management-e2e-progress.md`:
 *
 *   External-data import via CSV file import modal.
 */
class ImportShareSubscriptionsDataWorkflowTest {

    companion object {
        const val STORAGE_PATH = "ImportShareSubscriptionsDataWorkflowTest"
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val headless = TestConfig.HEADLESS
            val slowMo = TestConfig.SLOW_MO
            playwright = Playwright.create()
            browser = playwright.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setSlowMo(slowMo)
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
    @Develop
    // @Test Commented out
    fun `provider updates share subscriptions via file import modal`() {
        val reportDirectory = "ImportShareSubscriptionsDataWorkflowTest"
        val suffix = System.currentTimeMillis()

        val adminUsername = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
        val adminPassword = requireNotNull(System.getenv("TEST_USER_PASSWORD")) { "TEST_USER_PASSWORD must be set" }
        val adminUser = AdminTestUser(Username(adminUsername), Password(adminPassword), STORAGE_PATH)

        // 1. Create a test user via workflow
        val memberUsername = "e2e-import-user-$suffix@solyton.org"
        val testUser = withUser(adminUser on browser) {
            run(CreateTestUserWorkflow(this)) withInput
                CreateTestUserInput(
                    Username(memberUsername),
                    Password(generateSecurePassword()),
                    true,
                    "$suffix",
                    "$reportDirectory/admin-actions"
                )
        }

        // 2. Perform workflow as the test user
        withUser<Unit>(testUser on browser) {
            withFailureDiagnostics("$reportDirectory/user-actions") {
                dismissStrayErrorDialogIfPresent(true, "$reportDirectory/user-actions/dismissed-errors")
                val organizationName = "e2e-import-org-$suffix"
                val shareTypeName = "e2e-import-share-type-$suffix"
                val shareTypeKey = "imp-$suffix"
                val distributionPointName = "e2e-import-dp-$suffix"
                val fiscalYearKey = "3028"
                val initialPrice = "100"

                // Setup share environment
                run(SetupShareEnvironmentWorkflow(this)) withInput SetupShareEnvironmentInput(
                    organizationName = organizationName,
                    firstname = Firstname("Import"),
                    lastname = Lastname("User"),
                    fiscalYears = listOf(FiscalYearPeriod("$fiscalYearKey-01-01", "$fiscalYearKey-12-31")),
                    distributionPointName = distributionPointName,
                    shareTypeName = shareTypeName,
                    shareTypeKey = shareTypeKey,
                    shareTypeDescription = "E2E import fixture share type",
                    offers = listOf(
                        ShareOfferConfig(
                            fiscalYearKey = fiscalYearKey,
                            price = initialPrice,
                            ahcAuthorizationRequired = false
                        )
                    )
                )

                // Create initial subscription: 1 share at 100
                val subscription = run(CreateShareSubscriptionWorkflow(this)) withInput CreateShareSubscriptionInput(
                    memberName = "Import User",
                    fiscalYearKey = fiscalYearKey,
                    shareTypeName = shareTypeName,
                    distributionPointName = distributionPointName,
                    price = initialPrice,
                    quantity = "1",
                    ahcAuthorized = false
                )

                // Import CSV data modifying number of shares to 2 and price to 250
                val updatedSubscription = run(ImportShareSubscriptionsWorkflow(this)) withInput ImportShareSubscriptionsInput(
                    subscriptionId = subscription.id,
                    username = testUser.username.value,
                    newNumberOfShares = 2,
                    newPricePerShare = 250.0
                )

                // Verify updated values in subscription data object and UI list
                assertEquals(2, updatedSubscription.numberOfShares)
                assertEquals(250.0, updatedSubscription.pricePerShare)

                val sharesList = SharesPage(page).ShareSubscriptionManagement().listOfSubscriptions()
                assertThat(sharesList.itemText(subscription.id, "250")).isVisible()
            }
        }
    }
}
