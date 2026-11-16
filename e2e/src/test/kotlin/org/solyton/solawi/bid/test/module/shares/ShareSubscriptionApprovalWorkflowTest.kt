package org.solyton.solawi.bid.test.module.shares

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.coroutines.runBlocking
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.file.Paths
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.shares.data.api.ShareStatus
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.pageobjects.module.shares.SharesPage
import org.solyton.solawi.bid.state.*
import org.solyton.solawi.bid.workflow.admin.CreateTestUserInput
import org.solyton.solawi.bid.workflow.admin.CreateTestUserWorkflow
import org.solyton.solawi.bid.workflow.banking.FiscalYearPeriod
import org.solyton.solawi.bid.workflow.shares.*

/**
 * End-to-end coverage for the Share Management module, workflow B1 from
 * `.junie/progress/share-management-e2e-progress.md`:
 *
 *   PendingActivation --(PROVIDER, NO_PAYMENT_MANDATE_REQUIRED)--> Subscribed
 */
class ShareSubscriptionApprovalWorkflowTest {

    companion object {
        const val STORAGE_PATH = "ShareSubscriptionApprovalWorkflowTest"
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
    @Test
    fun `provider approves pending subscription without payment mandate` () {
        val reportDirectory = "ShareSubscriptionApprovalWorkflowTest"
        val suffix = System.currentTimeMillis()
        val report = E2eReport("Provider approves a pending subscription without payment mandate")

        val adminUsername = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
        val adminPassword = requireNotNull(System.getenv("TEST_USER_PASSWORD")) { "TEST_USER_PASSWORD must be set" }
        val adminUser = AdminTestUser(Username(adminUsername), Password(adminPassword), STORAGE_PATH)

        try {
            E2eEnvironment(browser).use { environment ->
                val initial = E2eState(
                    userContext = environment.open(adminUser),
                    environment = environment,
                    report = report
                )

                val createUser = CreateTestUserInput(
                    username = Username("e2e-user@solyton.org"),
                    password = Password(generateSecurePassword()),
                    useSuffixInUserName = true,
                    suffix = suffix.toString(),
                    reportDirectory = reportDirectory + "/admin-actions"
                )
                val setup = SetupShareEnvironmentInput(
                    organizationName = "e2e-shares-org-" + suffix,
                    firstname = Firstname("John"),
                    lastname = Lastname("Doe"),
                    fiscalYears = listOf(FiscalYearPeriod("3026-01-01", "3026-12-31")),
                    distributionPointName = "e2e-dp-" + suffix,
                    shareTypeName = "e2e-share-type-" + suffix,
                    shareTypeKey = "key-" + suffix,
                    shareTypeDescription = "E2E fixture share type",
                    offers = listOf(
                        ShareOfferConfig(
                            fiscalYearKey = "3026",
                            price = "100",
                            ahcAuthorizationRequired = false
                        )
                    )
                )
                val subscribe = CreateShareSubscriptionInput(
                    memberName = "John Doe",
                    fiscalYearKey = "3026",
                    shareTypeName = setup.shareTypeName,
                    distributionPointName = setup.distributionPointName,
                    price = "100",
                    quantity = "1",
                    ahcAuthorized = false
                )
                val approve = BulkChangeSubscriptionStatusInput(
                    filterStatus = "All Statuses",
                    newStatus = "SUBSCRIBED"
                )

                // Domain-readable workflow; the existing OOP Page Objects remain unchanged.
                // The returned values of one step feed the next using evoleq Kleisli composition.
                val scenario: E2eAction<Unit> = runBlocking {
                    workflowStep("Admin creates a test user", createUser, ::CreateTestUserWorkflow) *
                        { testUser: StdTestUser -> switchUser(testUser) } *
                        { _: Unit ->
                            workflowStep("Set up the share-management environment", setup, ::SetupShareEnvironmentWorkflow)
                        } *
                        { _: ShareEnvironmentSetupResult ->
                            workflowStep("Member subscribes to a share", subscribe, ::CreateShareSubscriptionWorkflow)
                        } *
                        { subscription: ApiShareSubscription ->
                            workflowStep("Provider approves the subscription", approve, ::BulkChangeSubscriptionStatusWorkflow) *
                                { subscriptions: ApiShareSubscriptions ->
                                    // Independent checks are accumulated rather than failing on the first assertion.
                                    verifyAll(
                                        expect("Subscription is marked SUBSCRIBED in domain data") {
                                            val actual = subscriptions.all.first { it.id == subscription.id }
                                            assertEquals(ShareStatus.Subscribed, actual.status)
                                        },
                                        expect("Subscription is visible as SUBSCRIBED in the user interface") {
                                            val list = SharesPage(userContext.page)
                                                .ShareSubscriptionManagement()
                                                .listOfSubscriptions()
                                            assertThat(list.itemStatus(subscription.id, "SUBSCRIBED")).isVisible()
                                        }
                                    )
                                }
                        }
                }
                runBlocking { scenario runOn initial }
            }
        } finally {
            report.writeTo(
                Paths.get("build/reports/tests/scenarios", "share-subscription-approval.md")
            )
        }
    }
}
