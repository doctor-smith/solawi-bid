package org.solyton.solawi.bid.test.module.shares

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.pageobjects.SharesPage
import org.solyton.solawi.bid.workflow.admin.CreateTestUserInput
import org.solyton.solawi.bid.workflow.admin.CreateTestUserWorkflow
import org.solyton.solawi.bid.workflow.banking.FiscalYearPeriod
import org.solyton.solawi.bid.workflow.shares.*

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
                val organizationName = "e2e-shares-org-b2-$suffix"
                val shareTypeName = "e2e-share-type-b2-$suffix"
                val shareTypeKey = "key-b2-$suffix"
                val distributionPointName = "e2e-dp-b2-$suffix"
                val fiscalYearKey = "3027"
                val price = "100"
                val collectionKey = "e2e-collection-$suffix"
                val mandateReferencePrefix = "E2E$suffix"

                // Setup entire share environment
                run(SetupShareEnvironmentWorkflow(this)) withInput SetupShareEnvironmentInput(
                    organizationName = organizationName,
                    firstname = Firstname("Jane"),
                    lastname = Lastname("Roe"),
                    fiscalYears = listOf(FiscalYearPeriod("$fiscalYearKey-01-01", "$fiscalYearKey-12-31")),
                    distributionPointName = distributionPointName,
                    shareTypeName = shareTypeName,
                    shareTypeKey = shareTypeKey,
                    shareTypeDescription = "E2E fixture share type (B2)",
                    offers = listOf(
                        ShareOfferConfig(
                            fiscalYearKey = fiscalYearKey,
                            price = price,
                            ahcAuthorizationRequired = true,
                            sepaCollectionConfig = SepaCollectionConfig(
                                collectionKey = collectionKey,
                                mandateReferencePrefix = mandateReferencePrefix,
                                remittanceInformation = "E2E fixture collection"
                            )
                        )
                    )
                )

                // Member subscription - starts out as PendingActivation
                val subscription = run(CreateShareSubscriptionWorkflow(this)) withInput CreateShareSubscriptionInput(
                    memberName = "Jane Roe",
                    fiscalYearKey = fiscalYearKey,
                    shareTypeName = shareTypeName,
                    distributionPointName = distributionPointName,
                    price = price,
                    quantity = "1",
                    ahcAuthorized = true,
                    attachSepaMandate = true
                )

                val sharesPage = SharesPage(page)
                val shareSubscriptionManagement = sharesPage.ShareSubscriptionManagement()
                shareSubscriptionManagement.open()

                val listOfSubscriptions = SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions(
                    locator = page.getByDataId("shares.page.share-subscription.list")
                )

                // We added just one mandate to the subscription
                val mandateEntry = listOfSubscriptions.item(subscription.id).getByText("$collectionKey / $mandateReferencePrefix")
                assertThat(mandateEntry).isVisible()
            }
        }
    }
}