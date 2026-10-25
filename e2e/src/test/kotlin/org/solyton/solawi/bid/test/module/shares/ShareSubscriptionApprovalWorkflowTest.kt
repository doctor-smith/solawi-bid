package org.solyton.solawi.bid.test.module.shares

import CHECK_FALSE
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.PhoneNumber
import org.solyton.solawi.bid.pageobjects.*
import java.nio.file.Files

/**
 * End-to-end coverage for the Share Management module, workflow B1 from
 * `.junie/progress/share-management-e2e-progress.md`:
 *
 *   PendingActivation --(PROVIDER, NO_PAYMENT_MANDATE_REQUIRED)--> Subscribed
 *
 * Unlike every other e2e test in this project, Share Management has no pre-seeded
 * organization to build on: `TEST_USER` has no organization with Banking, Distribution
 * or Share Management booked. This test therefore builds its own, fully isolated
 * fixture organization from scratch, purely by driving the admin UI (no direct
 * database/backend shortcuts), covering in order:
 *
 *   1. Creating a personal user profile (Personal Data page)
 *   2. Organization creation (`/app/management/organizations`)
 *   3. Booking Banking, Distribution and Share Management for it
 *      (`/app/management/private`, "connect" flow)
 *   4. Creating a fiscal year (Banking application page)
 *   5. Creating a share type and a fiscal-year-scoped share offer (Share Management page)
 *   6. Creating a distribution point (Distribution application page)
 *   7. Creating a member subscription to the offer (Share Management page)
 *   8. Approving the subscription via the "Bulk Edit" status-change dialog
 *
 * The UI actions themselves live in dedicated page-object classes under
 * `org.solyton.solawi.bid.pageobjects`, reusable by other e2e tests/modules. The full
 * click-by-click path (including every `data-id` used below, and the ones that had to
 * be added to production UI code to make this possible) is documented in
 * `.junie/progress/share-management-e2e-progress.md` and
 * `docs/e2e-fixture-workflows.md`.
 */
class ShareSubscriptionApprovalWorkflowTest {

    companion object {
        const val STORAGE_PATH = "ShareSubscriptionApprovalWorkflowTest"
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
            // Logs in once as TEST_USER (who becomes the owner/admin of every organization
            // it creates, see `OrganizationManagementPage.createOrganization`) and stores
            // the resulting session, so the test can reuse it.
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

    // A fresh context, already logged in as TEST_USER via the stored session.
    private fun adminContext(): BrowserContext {
        val storedState = Files.readString(TestUtils().storageStatePath(STORAGE_PATH))
        return browser.newContext(
            Browser.NewContextOptions().setStorageState(storedState)
        )
    }

    @Workflow
    @Test
    fun `provider approves pending subscription without payment mandate`() {
        val context = adminContext()
        try {
            val page = context.newPage()
            page.withFailureDiagnostics("ShareSubscriptionApprovalWorkflowTest") {
                val suffix = System.currentTimeMillis()
                val organizationName = "e2e-shares-org-$suffix"
                val shareTypeName = "e2e-share-type-$suffix"
                val shareTypeKey = "key-$suffix"
                val distributionPointName = "e2e-dp-$suffix"
                val username = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
                val fiscalYearKey = "3026"
                val price = "100"

                // profile
                val title = "Dr."
                val firstname = Firstname("John")
                val lastname = Lastname("Doe")
                val phoneNumber = PhoneNumber("0123456789")
                val mobileNumber = PhoneNumber("0123456789")
                // address
                val recipientName = "John Doe"
                val addressLine1 = "123 Main Street"
                val addressLine2 = "Apt 1"
                val city = "Anytown"
                val postalCode = "12345"
                val stateOrProvince = "DE-BW"
                val countryCode = "DE"

                val organizationManagementPage = OrganizationManagementPage(page)
                val privateApplicationManagementPage = PrivateApplicationManagementPage(page)
                val personalDataPage = PersonalDataPage(page)
                val bankingPage = BankingPage(page)
                val distributionPage = DistributionPage(page)
                val sharesPage = SharesPage(page)

                val moveToDashboard = {
                    page.getByDataId("navbar.apps-button").apply {
                        waitFor()
                        click()
                    }
                    page.waitForLoadingToFinish()
                }

                page.navigate("${TestConfig.BASE_URL}/app/dashboard")
                val personalDataCard = { page.getByDataId("page.dashboard.card.personal-data") }
                personalDataCard().waitFor()
                personalDataCard().click()
                personalDataPage.navigateTo(false)
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

                moveToDashboard()

                // Create organization
                // 1. click on organization card
                val organizationCard = { page.getByDataId("page.dashboard.card.organization-management") }
                organizationCard().apply {
                    waitFor()
                    click()
                }

                page.dismissStrayErrorDialogIfPresent()
                page.waitForLoadingToFinish()

                organizationManagementPage.createOrganization(organizationName)
                page.waitForLoadingToFinish()

                moveToDashboard()
                organizationCard().apply {
                    waitFor()
                }

                // 2. Book every application this fixture needs, then explicitly (re-)grant the
                // owner permission via the UI to refresh the client-side permission cache (see
                // `OrganizationManagementPage.grantOwnerPermission`).
                val myAppsButton = page.getByDataId("page.dashboard.application-management.my-apps")
                myAppsButton.click()
                page.waitForLoadingToFinish()
                page.dismissStrayErrorDialogIfPresent()
                page.waitForLoadingToFinish()

                privateApplicationManagementPage.bookApplication("BANKING", organizationName, false)
                privateApplicationManagementPage.bookApplication("DISTRIBUTION", organizationName, false)
                privateApplicationManagementPage.bookApplication("SHARE_MANAGEMENT", organizationName, false)

                organizationManagementPage.grantOwnerPermission(organizationName, "BANKING", username)
                organizationManagementPage.grantOwnerPermission(organizationName, "DISTRIBUTION", username)
                organizationManagementPage.grantOwnerPermission(organizationName, "SHARE_MANAGEMENT", username)

                // back to dashboard
                moveToDashboard()
                val bankingButton = page.getByDataId("page.dashboard.banking-application.organization-management")
                bankingButton.apply {
                    waitFor()
                    click()
                }
                // 3. Legal entity + creditor id (required for SEPA-related Banking operations),
                // then the fiscal year itself (required before any share offer can be created).
                page.navigateToOrganizationApplicationPage("banking", organizationName, false)

                // create Legal entity
                bankingPage.createLegalEntityWithCreditorId(organizationName, "e.V.", "DE98ZZZ09999999999")
                page.waitForLoadingToFinish()

                // create fiscal year
                bankingPage.createFiscalYear("$fiscalYearKey-01-01", "$fiscalYearKey-12-31")
                page.waitForLoadingToFinish()

                // back to dashboard
                moveToDashboard()

                // Distribution point (required to create a subscription)
                val distributionManagementLink = page.getByDataId("page.dashboard.distribution-management.organization-management")
                distributionManagementLink.apply {
                    waitFor()
                    click()
                }

                page.navigateToOrganizationApplicationPage("distribution", organizationName, false)
                distributionPage.createDistributionPoint(distributionPointName)
                page.waitForLoadingToFinish()

                moveToDashboard()

                // Share type + fiscal-year-scoped share offer.
                val shareManagementLink = page.getByDataId("page.dashboard.share-management.organization-management")
                shareManagementLink.apply {
                    waitFor()
                    click()
                }
                page.navigateToOrganizationApplicationPage("shares", organizationName, false)
                sharesPage.createShareType(shareTypeName, shareTypeKey, "E2E fixture share type")
                sharesPage.createShareOffer(fiscalYearKey, shareTypeName, price)

                // Member subscription (TEST_USER is automatically a member of the org it
                // just created) - starts out as PendingActivation.
                sharesPage.createShareSubscription(
                    username,
                    "${firstname.value} ${lastname.value}",
                    fiscalYearKey,
                    shareTypeName,
                    distributionPointName,
                    price,
                    "1",
                    false,
                )

                page.waitForLoadingToFinish()

                // set status filter to PENDING_ACTIVATION
                val statusFilterDropdown = page.getByDataId("shares.page.share-subscription.status-dropdown")
                statusFilterDropdown.click()
                val pendingStatusOption = page.getByDataId("shares.page.share-subscription.status-dropdown.option.PENDING_ACTIVATION")
                pendingStatusOption.click()
                PlaywrightAssertions.assertThat(pendingStatusOption).not().isVisible()

                // set ahc authorization filter to No
                val ahcAuthorizedFilterDropdown = page.getByDataId("shares.page.share-subscription.ahc-authorized-dropdown")
                ahcAuthorizedFilterDropdown.click()
                val ahcAuthorizedFilterNoOption = page.getByDataId("shares.page.share-subscription.ahc-authorized-dropdown.option.$CHECK_FALSE")
                ahcAuthorizedFilterNoOption.click()
                PlaywrightAssertions.assertThat(ahcAuthorizedFilterNoOption).not().isVisible()

                val shareSubscriptionList = page.getByDataId("shares.page.share-subscription.list")
                val pendingRow = shareSubscriptionList.getByText("PENDING_ACTIVATION")
                Assertions.assertTrue(
                    pendingRow.isVisible,
                    "The newly created subscription should start out as PENDING_ACTIVATION."
                )

                // Provider approval without a payment mandate: PendingActivation ->
                // (PROVIDER, NO_PAYMENT_MANDATE_REQUIRED) -> Subscribed.
                sharesPage.changeStatusViaBulkEdit("SUBSCRIBED")
                page.waitForLoadingToFinish()

                statusFilterDropdown.click()
                val subscribedStatusOption = page.getByDataId("shares.page.share-subscription.status-dropdown.option.SUBSCRIBED")
                subscribedStatusOption.click()
                PlaywrightAssertions.assertThat(subscribedStatusOption).not().isVisible()

                val subscribedRow = shareSubscriptionList.getByText("SUBSCRIBED")
                Assertions.assertTrue(
                    subscribedRow.isVisible,
                    "The subscription should become SUBSCRIBED after provider approval without a payment mandate."
                )
            }
        } finally {
            context.close()
        }
    }
}