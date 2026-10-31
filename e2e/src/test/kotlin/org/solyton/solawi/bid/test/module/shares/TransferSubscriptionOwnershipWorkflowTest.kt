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
import org.solyton.solawi.bid.workflow.banking.FiscalYearPeriod
import org.solyton.solawi.bid.workflow.organization.AddMemberToOrganizationInput
import org.solyton.solawi.bid.workflow.organization.AddMemberToOrganizationWorkflow
import org.solyton.solawi.bid.workflow.shares.*

/**
 * End-to-end coverage for the Share Management module: Ownership Transfer workflow
 *
 *   1. Erstellt initial ein Share-Abonnement für Benutzer A.
 *   2. Führt den Ownership-Transfer zu Benutzer B (neuer Eigentümer) durch.
 *   3. Verifiziert, dass das Abonnement nun dem neuen Benutzer zugeordnet ist
 *      und der Status erhalten bleibt.
 */
class TransferSubscriptionOwnershipWorkflowTest {

    companion object {
        const val STORAGE_PATH = "TransferSubscriptionOwnershipWorkflowTest"
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
    fun `provider transfers ownership of an existing share subscription to another member`() {
        val reportDirectory = "TransferSubscriptionOwnershipWorkflowTest"
        val suffix = System.currentTimeMillis()

        val adminUsername = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
        val adminPassword = requireNotNull(System.getenv("TEST_USER_PASSWORD")) { "TEST_USER_PASSWORD must be set" }
        val adminUser = AdminTestUser(Username(adminUsername), Password(adminPassword), STORAGE_PATH)

        // 1. Admin erstellt den Provider / Test-User und den neuen Eigentümer in einer Admin-Sitzung
        val (providerUser, newOwnerUser) = withUser(adminUser on browser) {
            withFailureDiagnostics("$reportDirectory/admin-actions") {
                val userManagementPage = org.solyton.solawi.bid.pageobjects.module.user.UserManagementPage(page)
                userManagementPage.load()
                dismissStrayErrorDialogIfPresent(true)

                val provider = userManagementPage.createUserViaAdminUi(
                    Username("e2e-provider-$suffix@solyton.org"),
                    Password(generateSecurePassword()),
                    useTimestamp = false
                )

                val newOwner = userManagementPage.createUserViaAdminUi(
                    Username("new-owner-$suffix@solyton.org"),
                    Password(generateSecurePassword()),
                    useTimestamp = false
                )

                org.solyton.solawi.bid.pageobjects.shared.NavBar(page).logout()
                page.waitForURL("**/login")
                dismissStrayErrorDialogIfPresent(true)

                Pair(provider, newOwner)
            }
        }

        // 3. Ablauf als Provider durchführen
        withUser(providerUser on browser) {
            withFailureDiagnostics("$reportDirectory/user-actions") {
                val organizationName = "e2e-shares-org-transfer-$suffix"
                val shareTypeName = "e2e-share-type-transfer-$suffix"
                val shareTypeKey = "key-transfer-$suffix".take(20)
                val distributionPointName = "e2e-dp-transfer-$suffix"
                val fiscalYearKey = "3030"
                val price = "120"
                val initialMemberName = "Provider Admin"
                val newOwnerUsername = newOwnerUser.username.value

                // Setup Share-Umgebung (Organisation, Fiskaljahr, Share Type & Offer)
                run(SetupShareEnvironmentWorkflow(this)) withInput SetupShareEnvironmentInput(
                    organizationName = organizationName,
                    firstname = Firstname("Provider"),
                    lastname = Lastname("Admin"),
                    fiscalYears = listOf(FiscalYearPeriod("$fiscalYearKey-01-01", "$fiscalYearKey-12-31")),
                    distributionPointName = distributionPointName,
                    shareTypeName = shareTypeName,
                    shareTypeKey = shareTypeKey,
                    shareTypeDescription = "E2E fixture for ownership transfer",
                    offers = listOf(
                        ShareOfferConfig(
                            fiscalYearKey = fiscalYearKey,
                            price = price,
                            ahcAuthorizationRequired = false
                        )
                    )
                )

                // Share Subscription für den ursprünglichen Eigentümer anlegen (PendingActivation)
                val subscription = run(CreateShareSubscriptionWorkflow(this)) withInput CreateShareSubscriptionInput(
                    memberName = initialMemberName,
                    fiscalYearKey = fiscalYearKey,
                    shareTypeName = shareTypeName,
                    distributionPointName = distributionPointName,
                    price = price,
                    quantity = "1",
                    ahcAuthorized = false
                )

                // Genehmigung auf SUBSCRIBED
                val subscribedList = run(BulkChangeSubscriptionStatusWorkflow(this)) withInput BulkChangeSubscriptionStatusInput(
                    filterStatus = "All Statuses",
                    newStatus = "SUBSCRIBED"
                )
                assertThat(subscribedList.itemStatus(subscription.id, "SUBSCRIBED")).isVisible()

                // Neuen Eigentümer als Mitglied zur Organisation via Organisationsseiten-Modal hinzufügen
                run(AddMemberToOrganizationWorkflow(this)) withInput AddMemberToOrganizationInput(
                    organizationName = organizationName,
                    username = newOwnerUsername,
                    firstname = "New",
                    lastname = "Owner"
                )

                // Zurück zu den Share-Abonnements der Organisation navigieren
                page.navigateToOrganizationApplicationPage("shares", organizationName, true)

                // Ownership Transfer auf neuen Eigentümer ausführen
                val transferredSubscription = run(TransferSubscriptionOwnershipWorkflow(this)) withInput TransferSubscriptionOwnershipInput(
                    subscriptionId = subscription.id,
                    newOwnerUsername = newOwnerUsername,
                    newOwnerFullname = "New Owner"
                )

                // Verifikation: Abonnement existiert weiterhin im Status SUBSCRIBED
                val listOfSubscriptions = org.solyton.solawi.bid.pageobjects.SharesPage(page).ShareSubscriptionManagement().listOfSubscriptions()
                assertThat(listOfSubscriptions.itemStatus(transferredSubscription.id, "SUBSCRIBED")).isVisible()
                assertThat(listOfSubscriptions.itemText(transferredSubscription.id, "New Owner")).isVisible()
            }
        }
    }
}
