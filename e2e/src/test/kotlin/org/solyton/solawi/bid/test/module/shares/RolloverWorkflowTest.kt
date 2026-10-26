
package org.solyton.solawi.bid.test.module.shares

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
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

                val organizationName = "e2e-shares-org-d1-$suffix"
                val shareTypeName = "e2e-share-type-d1-$suffix"
                val shareTypeKey = "key-d1-$suffix"
                val distributionPointName = "e2e-dp-d1-$suffix"
                val fiscalYearKey1 = "3028"
                val fiscalYearKey2 = "3029"
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
                with(privateApplicationManagementPage) {
                    bookApplication("BANKING", organizationName, false)
                    bookApplication("DISTRIBUTION", organizationName, false)
                    bookApplication("SHARE_MANAGEMENT", organizationName, false)
                }
                with(organizationManagementPage) {
                    grantOwnerPermission(
                        organizationName,
                        "BANKING",
                        user.username.value
                    )
                    grantOwnerPermission(
                        organizationName,
                        "DISTRIBUTION",
                        user.username.value
                    )
                    grantOwnerPermission(
                        organizationName,
                        "SHARE_MANAGEMENT",
                        user.username.value
                    )
                }
                // Setup Banking (Legal Entity, 2 Fiscal Years, Creditor & Debtor Bank Account)
                navBar.moveToDashboard()
                dashboardPage.bankingOrganizationManagementLink().click()
                page.navigateToOrganizationApplicationPage("banking", organizationName, false)

                with(bankingPage) {
                    createLegalEntityWithCreditorId(organizationName, "e.V.", "DE98ZZZ09999999999")
                    waitForLoadingToFinish()

                    createFiscalYear("$fiscalYearKey1-01-01", "$fiscalYearKey1-12-31")
                    waitForLoadingToFinish()
                    createFiscalYear("$fiscalYearKey2-01-01", "$fiscalYearKey2-12-31")
                    waitForLoadingToFinish()


                    createCreditorBankAccount(
                        accountHolder = organizationName,
                        iban = "DE02100100100006820101",
                        bic = "PBNKDEFFXXX",
                        description = "e2e-creditor-account"
                    )
                    waitForLoadingToFinish()

                    createDebtorBankAccount(
                        username = user.username.value,
                        accountHolder = "${firstname.value} ${lastname.value}",
                        iban = "DE89370400440532013000",
                        bic = "COBADEFFXXX"
                    )
                    waitForLoadingToFinish()
                }
                // Setup Distribution
                navBar.moveToDashboard()
                dashboardPage.distributionOrganizationManagementLink().click()

                page.navigateToOrganizationApplicationPage("distribution", organizationName, false)
                with(distributionPage) {
                    createDistributionPoint(distributionPointName)
                    waitForLoadingToFinish()
                }


                // Setup Shares: Create Share Type, FY1 Offer, FY2 Offer and attach SEPA Collections
                navBar.moveToDashboard()
                dashboardPage.shareManagementOrganizationManagementLink().click()
                page.navigateToOrganizationApplicationPage("shares", organizationName, false)

                with(sharesPage) {
                    createShareType(shareTypeName, shareTypeKey, "E2E fixture share type (D1)")

                    // FY1 Offer + SEPA Collection
                    val shareOffer1 = createShareOffer(fiscalYearKey1, shareTypeName, price, ahcAuthorizationRequired = false)
                    val collectionKey1 = "D1-FY1-$suffix"
                    val mandateReferencePrefix1 = "D1A"
                    attachSepaCollectionToOffer(
                        shareOffer = shareOffer1,
                        creditorAccountHolder = organizationName,
                        creditorAccountDescription = "e2e-creditor-account",
                        collectionKey = collectionKey1,
                        mandateReferencePrefix = mandateReferencePrefix1,
                        remittanceInformation = "E2E fixture collection FY1"
                    )
                    waitForLoadingToFinish()

                    // FY2 Offer + SEPA Collection (required so the rollover dropdown in Bulk-Edit lists it)
                    val shareOffer2 = createShareOffer(fiscalYearKey2, shareTypeName, price, ahcAuthorizationRequired = false)

                    val collectionKey2 = "D1-FY2-$suffix"
                    val mandateReferencePrefix2 = "D1A"
                    attachSepaCollectionToOffer(
                        shareOffer = shareOffer2,
                        creditorAccountHolder = organizationName,
                        creditorAccountDescription = "e2e-creditor-account",
                        collectionKey = collectionKey2,
                        mandateReferencePrefix = mandateReferencePrefix2,
                        remittanceInformation = "E2E fixture collection FY2"
                    )
                    waitForLoadingToFinish()

                    // Create Share Subscription in FY1
                    createShareSubscription(
                        user.username.value,
                        "${firstname.value} ${lastname.value}",
                        fiscalYearKey1,
                        shareTypeName,
                        distributionPointName,
                        price,
                        "1",
                        false
                    )
                    waitForLoadingToFinish()


                    val shareSubscriptionManagement = ShareSubscriptionManagement()
                    shareSubscriptionManagement.open()

                    val statusFilter = SharesPage.ShareSubscriptionManagement.StatusFilter(
                        locator = shareSubscriptionManagement.locator.getByDataId("shares.page.share-subscription.status-dropdown.wrapper")
                    )
                    assertThat(statusFilter).isVisible()

                    val ahcAuthFilter = SharesPage.ShareSubscriptionManagement.AhcAuthFilter(
                        locator = shareSubscriptionManagement.locator.getByDataId("shares.page.share-subscription.ahc-authorized-dropdown.wrapper")
                    )
                    assertThat(ahcAuthFilter).isVisible()

                    statusFilter.select("All Statuses")
                    waitForLoadingToFinish()
                    statusFilter.close()

                    assertFalse(statusFilter.isExpanded())

                    ahcAuthFilter.select("All Authorized")
                    waitForLoadingToFinish()
                    ahcAuthFilter.close()
                    assertFalse(ahcAuthFilter.isExpanded())


                    // Approve subscription -> transition from PENDING_ACTIVATION to SUBSCRIBED
                    changeStatusViaBulkEdit("SUBSCRIBED")
                    waitForLoadingToFinish()

                    // Perform Rollover to next season (FY2 Offer)
                    val targetOfferOptionText = "$fiscalYearKey2 - $shareTypeName"
                    rolloverViaBulkEdit(targetOfferOptionText)
                    waitForLoadingToFinish()

                    // Assertions:
                    shareSubscriptionManagement.open()


                    assertThat(statusFilter).isVisible()
                    statusFilter.select("All Statuses")
                    waitForLoadingToFinish()
                    statusFilter.close()

                    ahcAuthFilter.select("All Authorized")
                    waitForLoadingToFinish()
                    ahcAuthFilter.close()
                    assertFalse(ahcAuthFilter.isExpanded())

                    val shareSubscriptionList = page.getByDataId("shares.page.share-subscription.list")

                    // 1. Original subscription in FY1 must be in status ROLLING_OVER
                    val rollingOverRow = shareSubscriptionList.getByText("ROLLING_OVER")
                    Assertions.assertTrue(
                        rollingOverRow.isVisible,
                        "The original subscription should be in ROLLING_OVER state after rollover."
                    )

                    // 2. Newly created shadow subscription in FY2 must be in status PENDING_ACTIVATION
                    val pendingRow = shareSubscriptionList.getByText("PENDING_ACTIVATION")
                    Assertions.assertTrue(
                        pendingRow.isVisible,
                        "The new shadow subscription should be in PENDING_ACTIVATION state."
                    )
                }
            }
        }
    }
}
