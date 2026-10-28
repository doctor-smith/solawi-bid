package org.solyton.solawi.bid.test.module.shares

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.values.*
import org.solyton.solawi.bid.pageobjects.*
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.module.authentication.LoginPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar
import org.solyton.solawi.bid.workflow.admin.CreateTestUserInput
import org.solyton.solawi.bid.workflow.admin.CreateTestUserWorkflow

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
                val navBar = NavBar(page)
                val loginPage = LoginPage(page)
                val dashboardPage = DashboardPage(page)
                val personalDataPage = PersonalDataPage(page)
                val organizationManagementPage = OrganizationManagementPage(page)
                val privateApplicationManagementPage = PrivateApplicationManagementPage(page)
                val bankingPage = BankingPage(page)
                val distributionPage = DistributionPage(page)
                val sharesPage = SharesPage(page)

                // Profile data
                val title = "Dr."
                val firstname = Firstname("Jane")
                val lastname = Lastname("Doe")
                val phoneNumber = PhoneNumber("0123456789")
                val mobileNumber = PhoneNumber("0123456789")
                val recipientName = "Jane Doe"
                val addressLine1 = "123 Main Street"
                val addressLine2 = "Apt 2"
                val city = "Anytown"
                val postalCode = "12345"
                val stateOrProvince = "DE-BW"
                val countryCode = "DE"

                val organizationName = "e2e-shares-org-b4-$suffix"
                val shareTypeName = "e2e-share-type-b4-$suffix"
                val shareTypeKey = "key-b4-$suffix"
                val distributionPointName = "e2e-dp-b4-$suffix"
                val fiscalYearKey = "3028"
                val price = "100"

                // Login
                loginPage.load()
                waitForLoadingToFinish()
                page.clickOn("cookie-disclaimer.modal.submit-button")

                loginPage.login(user)
                page.waitForURL("**/dashboard")

                // Create profile
                val personalDataCard = dashboardPage.privateDataCard()
                assertThat(personalDataCard).isVisible()
                personalDataCard.click()
                waitForLoadingToFinish()

                personalDataPage.createProfile(
                    title, firstname, lastname, phoneNumber, mobileNumber,
                    recipientName, organizationName, addressLine1, addressLine2,
                    postalCode, city, countryCode, stateOrProvince
                )
                navBar.moveToDashboard()

                // Create organization
                val organizationCard = dashboardPage.organizationsCard()
                assertThat(organizationCard).isVisible()
                organizationCard.click()
                dismissStrayErrorDialogIfPresent()
                waitForLoadingToFinish()

                organizationManagementPage.createOrganization(organizationName)
                waitForLoadingToFinish()

                navBar.moveToDashboard()
                assertThat(organizationCard).isVisible()

                // Book applications & grant permissions
                dashboardPage.myAppsLink().click()
                waitForLoadingToFinish()
                dismissStrayErrorDialogIfPresent()
                waitForLoadingToFinish()

                privateApplicationManagementPage.bookApplication("BANKING", organizationName, false)
                privateApplicationManagementPage.bookApplication("DISTRIBUTION", organizationName, false)
                privateApplicationManagementPage.bookApplication("SHARE_MANAGEMENT", organizationName, false)

                organizationManagementPage.grantOwnerPermission(organizationName, "BANKING", user.username.value)
                organizationManagementPage.grantOwnerPermission(organizationName, "DISTRIBUTION", user.username.value)
                organizationManagementPage.grantOwnerPermission(
                    organizationName,
                    "SHARE_MANAGEMENT",
                    user.username.value
                )

                // Setup Banking (Legal Entity, Fiscal Year, Creditor & Debtor Bank Account)
                navBar.moveToDashboard()
                dashboardPage.bankingOrganizationManagementLink().click()
                page.navigateToOrganizationApplicationPage("banking", organizationName, false)

                bankingPage.createLegalEntityWithCreditorId(organizationName, "e.V.", "DE98ZZZ09999999999")
                waitForLoadingToFinish()

                bankingPage.createFiscalYear("$fiscalYearKey-01-01", "$fiscalYearKey-12-31")
                waitForLoadingToFinish()

                bankingPage.createCreditorBankAccount(
                    accountHolder = organizationName,
                    iban = "DE02100100100006820101",
                    bic = "PBNKDEFFXXX",
                    description = "e2e-creditor-account"
                )
                waitForLoadingToFinish()

                bankingPage.createDebtorBankAccount(
                    username = user.username.value,
                    accountHolder = "${firstname.value} ${lastname.value}",
                    iban = "DE89370400440532013000",
                    bic = "COBADEFFXXX"
                )
                waitForLoadingToFinish()

                // Setup Distribution
                navBar.moveToDashboard()
                dashboardPage.distributionOrganizationManagementLink().click()

                page.navigateToOrganizationApplicationPage("distribution", organizationName, false)
                distributionPage.createDistributionPoint(distributionPointName)
                waitForLoadingToFinish()

                // Setup Shares
                navBar.moveToDashboard()
                dashboardPage.shareManagementOrganizationManagementLink().click()
                page.navigateToOrganizationApplicationPage("shares", organizationName, false)

                with(sharesPage) {
                    createShareType(shareTypeName, shareTypeKey, "E2E fixture share type (B4)")
                    createShareOffer(fiscalYearKey, shareTypeName, price, ahcAuthorizationRequired = false)
                    waitForLoadingToFinish()

                    // Create Share Subscription (starts PENDING_ACTIVATION)
                    val subscription = createShareSubscription(
                        user.username.value,
                        "${firstname.value} ${lastname.value}",
                        fiscalYearKey,
                        shareTypeName,
                        distributionPointName,
                        price,
                        "1",
                        true
                    )
                    waitForLoadingToFinish()

                    val shareSubscriptions = ShareSubscriptionManagement()
                    shareSubscriptions.open()

                    val statusFilter = SharesPage.ShareSubscriptionManagement.StatusFilter(
                        locator = shareSubscriptions.locator.getByDataId("shares.page.share-subscription.status-dropdown.wrapper")
                    )
                    assertThat(statusFilter).isVisible()
                    statusFilter.select("All Statuses")
                    waitForLoadingToFinish()
                    statusFilter.close()

                    val listOfSubscriptions = SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions(
                        locator = page.getByDataId("shares.page.share-subscription.list")
                    )

                    // 1. Hop: PendingActivation -> CLEARED_FOR_AUCTION
                    val clearedSubscriptions = sharesPage.changeStatusViaBulkEdit("CLEARED_FOR_AUCTION")
                    assertTrue {
                        clearedSubscriptions.all.size == 1
                    }
                    waitForLoadingToFinish()

                    statusFilter.select("All Statuses")
                    waitForLoadingToFinish()
                    statusFilter.close()

                    val clearedRow = listOfSubscriptions.item(subscription.id).getByText("CLEARED_FOR_AUCTION")
                    Assertions.assertTrue(
                        clearedRow.isVisible,
                        "The subscription should be in CLEARED_FOR_AUCTION state."
                    )

                    // 2. Hop: ClearedForAuction -> SUBSCRIBED
                    val subscribedSubscriptions = sharesPage.changeStatusViaBulkEdit("SUBSCRIBED")
                    assertTrue {
                        subscribedSubscriptions.all.size == 1
                    }
                    waitForLoadingToFinish()

                    statusFilter.select("All Statuses")
                    waitForLoadingToFinish()
                    statusFilter.close()

                    val subscribedRow = listOfSubscriptions.item(subscription.id).getByText("SUBSCRIBED")
                    Assertions.assertTrue(
                        subscribedRow.isVisible,
                        "The subscription should be in SUBSCRIBED state."
                    )
                }
            }
        }
    }
}