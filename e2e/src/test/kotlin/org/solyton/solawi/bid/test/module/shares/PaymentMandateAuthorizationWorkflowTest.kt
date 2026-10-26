package org.solyton.solawi.bid.test.module.shares

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
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
 * End-to-end coverage for the Share Management module, workflow B2 from
 * `.junie/progress/share-management-e2e-progress.md`:
 *
 *   PendingActivation --(PROVIDER, ...)--> AwaitingAhcAuthorization --(..., ...)--> Subscribed
 *
 * Builds the same kind of isolated fixture organization as
 * `ShareSubscriptionApprovalWorkflowTest` (B1), reusing the same page objects, but with
 * a share offer that *requires* a SEPA payment-mandate authorization. On top of B1's
 * fixture, this additionally needs:
 *
 *   - A `CREDITOR` bank account for the organization itself.
 *   - A `DEBTOR` bank account for the subscribing member (TEST_USER).
 *   - A SEPA collection attached to the share offer.
 *   - A SEPA mandate attached to the resulting subscription.
 */
class PaymentMandateAuthorizationWorkflowTest {

    companion object {
        const val STORAGE_PATH = "PaymentMandateAuthorizationWorkflowTest"

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
    fun `provider approves pending subscription with payment mandate authorization`() {
        val reportDirectory = "PaymentMandateAuthorizationWorkflowTest"
        val suffix = System.currentTimeMillis()

        val adminUsername = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
        val adminPassword = requireNotNull(System.getenv("TEST_USER_PASSWORD")) { "TEST_USER_PASSWORD must be set" }
        val adminUser = AdminTestUser(Username(adminUsername), Password(adminPassword), STORAGE_PATH)

        // Create a test user and log out again
        val testUser = withUser(adminUser on browser) {
            run (CreateTestUserWorkflow(this)) withInput
                CreateTestUserInput(
                    Username("e2e-user@solyton.org"),
                    Password(generateSecurePassword()),
                    true,
                    "$suffix",
                    "$reportDirectory/admin-actions"
                )
        }

        // Run the test with the recently created test user
        withUser(testUser on browser) {
            withFailureDiagnostics("$reportDirectory/user-actions") {

                // used pages
                val navBar = NavBar(page)
                val loginPage = LoginPage(page)
                val dashboardPage = DashboardPage(page)
                val personalDataPage = PersonalDataPage(page)
                val organizationManagementPage = OrganizationManagementPage(page)
                val privateApplicationManagementPage = PrivateApplicationManagementPage(page)
                val bankingPage = BankingPage(page)
                val distributionPage = DistributionPage(page)
                val sharesPage = SharesPage(page)

                // necessary data

                // profile
                val title = "Dr."
                val firstname = Firstname("Jane")
                val lastname = Lastname("Roe")
                val phoneNumber = PhoneNumber("0123456789")
                val mobileNumber = PhoneNumber("0123456789")
                // address
                val recipientName = "Jane Roe"
                val addressLine1 = "123 Main Street"
                val addressLine2 = "Apt 1"
                val city = "Anytown"
                val postalCode = "12345"
                val stateOrProvince = "DE-BW"
                val countryCode = "DE"

                // organization
                val organizationName = "e2e-shares-org-b2-$suffix"

                // shares
                val shareTypeName = "e2e-share-type-b2-$suffix"
                val shareTypeKey = "key-b2-$suffix"
                val distributionPointName = "e2e-dp-b2-$suffix"
                val fiscalYearKey = "3027"
                val price = "100"



                // Login as e2e user
                loginPage.load()
                waitForLoadingToFinish()

                // dismiss cookie-disclaimer
                page.clickOn("cookie-disclaimer.modal.submit-button")

                loginPage.login(user)
                page.waitForURL("**/dashboard")

                // Create Userprofile
                val personalDataCard = dashboardPage.privateDataCard()
                assertThat(personalDataCard).isVisible()
                personalDataCard.click()

                waitForLoadingToFinish()

                personalDataPage.createProfile(
                    title,
                    firstname,
                    lastname,
                    phoneNumber,
                    mobileNumber,
                    recipientName,
                    organizationName,
                    addressLine1,
                    addressLine2,
                    postalCode,
                    city,
                    countryCode,
                    stateOrProvince
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

                // Book every application this fixture needs, then explicitly (re-)grant the
                // owner permission via the UI to refresh the client-side permission cache.
                dashboardPage.myAppsLink().click()
                waitForLoadingToFinish()
                dismissStrayErrorDialogIfPresent()
                waitForLoadingToFinish()

                privateApplicationManagementPage.bookApplication("BANKING", organizationName, false)
                privateApplicationManagementPage.bookApplication("DISTRIBUTION", organizationName, false)
                privateApplicationManagementPage.bookApplication("SHARE_MANAGEMENT", organizationName, false)

                organizationManagementPage.grantOwnerPermission(organizationName, "BANKING", user.username.value)
                organizationManagementPage.grantOwnerPermission(organizationName, "DISTRIBUTION", user.username.value)
                organizationManagementPage.grantOwnerPermission(organizationName, "SHARE_MANAGEMENT", user.username.value)

                // back to dashboard
                navBar.moveToDashboard()
                dashboardPage.bankingOrganizationManagementLink().click()
                // Legal entity + creditor id, fiscal year, and (new for B2) a creditor bank
                // account for the organization itself.
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

                // (new for B2) a debtor bank account for the subscribing member.
                bankingPage.createDebtorBankAccount(
                    username = user.username.value,
                    accountHolder = "${firstname.value} ${lastname.value}",
                    iban = "DE89370400440532013000",
                    bic = "COBADEFFXXX"
                )
                waitForLoadingToFinish()

                // back to dashboard
                navBar.moveToDashboard()

                // Distribution point (required to create a subscription)
                dashboardPage.distributionOrganizationManagementLink().click()

                page.navigateToOrganizationApplicationPage("distribution", organizationName, false)
                distributionPage.createDistributionPoint(distributionPointName)
                waitForLoadingToFinish()

                navBar.moveToDashboard()

                // Share type + fiscal-year-scoped share offer, this time requiring SEPA
                // authorization, then a SEPA collection attached to it.
                dashboardPage.shareManagementOrganizationManagementLink().click()
                page.navigateToOrganizationApplicationPage("shares", organizationName, false)
                sharesPage.createShareType(shareTypeName, shareTypeKey, "E2E fixture share type (B2)")
                val shareOffer = sharesPage.createShareOffer(fiscalYearKey, shareTypeName, price, ahcAuthorizationRequired = true)

                val collectionKey = "e2e-collection-$suffix"
                val mandateReferencePrefix = "E2E$suffix"

                sharesPage.attachSepaCollectionToOffer(
                    shareOffer = shareOffer,
                    creditorAccountHolder = organizationName,
                    creditorAccountDescription = "e2e-creditor-account",
                    collectionKey = collectionKey,
                    mandateReferencePrefix = mandateReferencePrefix,
                    remittanceInformation = "E2E fixture collection"
                )
                waitForLoadingToFinish()

                // Member subscription - starts out as PendingActivation.
                sharesPage.createShareSubscription(
                    user.username.value,
                    "${firstname.value} ${lastname.value}",
                    fiscalYearKey,
                    shareTypeName,
                    distributionPointName,
                    price,
                    "1",
                    true,
                )
                waitForLoadingToFinish()


                // set status filter to PENDING_ACTIVATION
                val statusFilterDropdown = page.getByDataId("shares.page.share-subscription.status-dropdown")
                statusFilterDropdown.click()
                val pendingStatusOption = page.getByDataId("shares.page.share-subscription.status-dropdown.option.PENDING_ACTIVATION")
                pendingStatusOption.click()
                assertThat(pendingStatusOption).not().isVisible()

                val shareSubscriptionList = page.getByDataId("shares.page.share-subscription.list")
                val pendingRow = shareSubscriptionList.getByText("PENDING_ACTIVATION")
                Assertions.assertTrue(
                    pendingRow.isVisible,
                    "The newly created subscription should start out as PENDING_ACTIVATION."
                )

                // Attach a SEPA mandate to the subscription - this authorizes the payment mandate
                sharesPage.attachSepaMandateToSubscription()
                waitForLoadingToFinish()

                // We added just one mandate to the subscription
                val list = page.getByDataId("shares.page.share-subscription.list")
                val mandateEntry = list.getByText("$collectionKey / $mandateReferencePrefix")
                assertThat(mandateEntry).isVisible()

                // TODO Think about navigating to the related banking page and check
                //     - if the sepa collection is listed
                //     - if the sepa collection is active
                //     - if the users sepa mandate is there
            }
        }
    }
}