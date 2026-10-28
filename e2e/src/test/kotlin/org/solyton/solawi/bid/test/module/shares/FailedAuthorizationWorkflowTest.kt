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
 * End-to-end coverage for the Share Management module, workflow B3 from
 * `.junie/progress/share-management-e2e-progress.md`:
 *
 *   PendingActivation -> AwaitingAhcAuthorization -> (PROVIDER, AUTHORIZATION_FAILED) -> Suspended
 */
class FailedAuthorizationWorkflowTest {

    companion object {
        const val STORAGE_PATH = "FailedAuthorizationWorkflowTest"

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
    fun `provider transitions pending subscription to suspended upon failed mandate authorization`() {
        val reportDirectory = "FailedAuthorizationWorkflowTest"
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
                val organizationName = "e2e-shares-org-b3-$suffix"
                val shareTypeName = "e2e-share-type-b3-$suffix"
                val shareTypeKey = "key-b3-$suffix"
                val distributionPointName = "e2e-dp-b3-$suffix"
                val fiscalYearKey = "3028"
                val price = "120"
                val collectionKey = "B3-$suffix"
                val mandateReferencePrefix = "B3"

                // Setup entire share environment (offer with SEPA collection requiring AHC authorization)
                run(SetupShareEnvironmentWorkflow(this)) withInput SetupShareEnvironmentInput(
                    organizationName = organizationName,
                    fiscalYears = listOf(FiscalYearPeriod("$fiscalYearKey-01-01", "$fiscalYearKey-12-31")),
                    distributionPointName = distributionPointName,
                    shareTypeName = shareTypeName,
                    shareTypeKey = shareTypeKey,
                    shareTypeDescription = "E2E fixture share type (B3)",
                    offers = listOf(
                        ShareOfferConfig(
                            fiscalYearKey = fiscalYearKey,
                            price = price,
                            ahcAuthorizationRequired = true,
                            sepaCollectionConfig = SepaCollectionConfig(
                                collectionKey = collectionKey,
                                mandateReferencePrefix = mandateReferencePrefix,
                                remittanceInformation = "E2E fixture collection (B3)"
                            )
                        )
                    )
                )

                // Create Share Subscription with SEPA Mandate attached
                run(CreateShareSubscriptionWorkflow(this)) withInput CreateShareSubscriptionInput(
                    memberName = "Jane Doe",
                    fiscalYearKey = fiscalYearKey,
                    shareTypeName = shareTypeName,
                    distributionPointName = distributionPointName,
                    price = price,
                    quantity = "1",
                    ahcAuthorized = true,
                    attachSepaMandate = true
                )

                // Transition to AWAITING_AHC_AUTHORIZATION
                val awaitingList = run(BulkChangeSubscriptionStatusWorkflow(this)) withInput BulkChangeSubscriptionStatusInput(
                    filterStatus = "All Statuses",
                    newStatus = "AWAITING_AHC_AUTHORIZATION"
                )
                val waitingForAuthorizationRow = awaitingList.locator.getByText("AWAITING_AHC_AUTHORIZATION")
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(waitingForAuthorizationRow).isVisible()

                // Provider sets status to SUSPENDED via Bulk Edit (reason: AUTHORIZATION_FAILED)
                val suspendedList = run(BulkChangeSubscriptionStatusWorkflow(this)) withInput BulkChangeSubscriptionStatusInput(
                    filterStatus = "All Statuses",
                    newStatus = "SUSPENDED"
                )
                val suspendedRow = suspendedList.locator.getByText("SUSPENDED")
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(suspendedRow).isVisible()
            }
        }
    }
}
