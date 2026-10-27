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
 * End-to-end coverage for the Share Management module, workflow D2:
 *
 *   Shadow Subscription: PendingActivation (FY2) -> Subscribed (FY2)
 *   Original Subscription: RollingOver (FY1) -> (automatic companion transition) -> RolledOver (FY1)
 */
class RolloverActivationWorkflowTest {

    companion object {
        const val STORAGE_PATH = "RolloverActivationWorkflowTest"

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
    fun `provider activates shadow subscription moving original subscription to rolled over`() {
        val reportDirectory = "RolloverActivationWorkflowTest"
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

                val organizationName = "e2e-shares-org-d2-$suffix"
                val shareTypeName = "e2e-share-type-d2-$suffix"
                val shareTypeKey = "key-d2-$suffix"
                val distributionPointName = "e2e-dp-d2-$suffix"
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
                    grantOwnerPermission(organizationName, "BANKING", user.username.value)
                    grantOwnerPermission(organizationName, "DISTRIBUTION", user.username.value)
                    grantOwnerPermission(organizationName, "SHARE_MANAGEMENT", user.username.value)
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
                    createShareType(shareTypeName, shareTypeKey, "E2E fixture share type (D2)")

                    // FY1 Offer + SEPA Collection
                    val shareOffer1 = createShareOffer(fiscalYearKey1, shareTypeName, price, ahcAuthorizationRequired = false)
                    val collectionKey1 = "D2-FY1-$suffix"
                    val mandateReferencePrefix1 = "D2A"
                    attachSepaCollectionToOffer(
                        shareOffer = shareOffer1,
                        creditorAccountHolder = organizationName,
                        creditorAccountDescription = "e2e-creditor-account",
                        collectionKey = collectionKey1,
                        mandateReferencePrefix = mandateReferencePrefix1,
                        remittanceInformation = "E2E fixture collection FY1"
                    )
                    waitForLoadingToFinish()

                    // FY2 Offer + SEPA Collection
                    val shareOffer2 = createShareOffer(fiscalYearKey2, shareTypeName, price, ahcAuthorizationRequired = false)
                    val collectionKey2 = "D2-FY2-$suffix"
                    val mandateReferencePrefix2 = "D2B"
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

                    // 1. Approve subscription -> transition from PENDING_ACTIVATION to SUBSCRIBED
                    changeStatusViaBulkEdit("SUBSCRIBED")
                    waitForLoadingToFinish()

                    // 2. Perform Rollover to next season (FY2 Offer)
                    val targetOfferOptionText = "$fiscalYearKey2 - $shareTypeName"
                    rolloverViaBulkEdit(targetOfferOptionText)
                    waitForLoadingToFinish()

                    // 3. Filter to PENDING_ACTIVATION to isolate the newly created shadow subscription for FY2
                    shareSubscriptionManagement.open()
                    statusFilter.select("PENDING_ACTIVATION")
                    waitForLoadingToFinish()
                    statusFilter.close()

                    // 4. Activate the shadow subscription via Bulk Edit
                    changeStatusViaBulkEdit("SUBSCRIBED")
                    waitForLoadingToFinish()

                    // 5. Reset status filter to check all subscriptions
                    statusFilter.select("All Statuses")
                    waitForLoadingToFinish()
                    statusFilter.close()

                    val shareSubscriptionList = page.getByDataId("shares.page.share-subscription.list")

                    // Assertions:
                    // a) The shadow subscription is now SUBSCRIBED
                    val subscribedRow = shareSubscriptionList.getByText("SUBSCRIBED")
                    Assertions.assertTrue(
                        subscribedRow.isVisible,
                        "The shadow subscription should be in SUBSCRIBED state."
                    )

                    // b) The original subscription has transitioned automatically to ROLLED_OVER
                    val rolledOverRow = shareSubscriptionList.getByText("ROLLED_OVER")
                    Assertions.assertTrue(
                        rolledOverRow.isVisible,
                        "The original subscription should have automatically transitioned to ROLLED_OVER."
                    )
                }
            }
        }
    }
}
