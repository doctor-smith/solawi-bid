package org.solyton.solawi.bid.test.module.auction

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.atTime
import org.evoleq.kotlinx.date.plusDays
import org.evoleq.kotlinx.date.today
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.bid.data.api.AuctionDetails
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.module.auction.AuctionsPage
import org.solyton.solawi.bid.pageobjects.module.user.OrganizationManagementPage
import org.solyton.solawi.bid.pageobjects.module.user.UserManagementPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar
import org.solyton.solawi.bid.workflow.auction.CreateAuctionInput
import org.solyton.solawi.bid.workflow.auction.CreateAuctionWorkflow
import org.solyton.solawi.bid.workflow.auction.UpdateAuctionFromAuctionsPageWorkflow
import org.solyton.solawi.bid.workflow.auction.UpdateAuctionInput
import org.solyton.solawi.bid.workflow.organization.*
import org.solyton.solawi.bid.workflow.user.UserLoginInput
import org.solyton.solawi.bid.workflow.user.UserLoginWorkflow

class CreateAuctionWorkflowTest {

    companion object {
        const val STORAGE_PATH = "CreateAuctionWorkflowTest"
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
    fun `provider creates new auction of solawi-tuebingen type`() {
        val reportDirectory = STORAGE_PATH
        val suffix = System.currentTimeMillis()

        val adminUsername = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
        val adminPassword = requireNotNull(System.getenv("TEST_USER_PASSWORD")) { "TEST_USER_PASSWORD must be set" }
        val adminUser = AdminTestUser(Username(adminUsername), Password(adminPassword), STORAGE_PATH)

        // 1. Admin creates Provider / Test-User
        val providerUser = withUser(adminUser on browser) {
            withFailureDiagnostics("$reportDirectory/admin-actions") {
                val userManagementPage = UserManagementPage(page)
                userManagementPage.load()
                dismissStrayErrorDialogIfPresent(true)

                val provider = userManagementPage.createUserViaAdminUi(
                    Username("e2e-auctions-provider-$suffix@solyton.org"),
                    Password(generateSecurePassword()),
                    useTimestamp = false
                )

                NavBar(page).logout()
                page.waitForURL("**/login")
                dismissStrayErrorDialogIfPresent(true)

                provider
            }
        }

        // 2. Perform actions as Provider: Create organization and add member
        withUser(providerUser on browser) {
            withFailureDiagnostics("$reportDirectory/user-actions") {
                run(UserLoginWorkflow(this)) withInput UserLoginInput()

                val organizationName = "e2e-create-auction-org-$suffix"
                run(CreateOrganizationWorkflow(this)) withInput CreateOrganizationInput(
                    organizationName = organizationName,
                    appsToBook = ApplicationBookingConfig.from(
                        "BANKING",
                        "DISTRIBUTION",
                        "SHARE_MANAGEMENT",
                        "AUCTIONS"
                    )
                )

                val memberUsername = "e2e-auctions-orga-member-$suffix@solyton.org"
                val memberFirstname = "John"
                val memberLastname = "Doe"

                run(AddMemberToOrganizationWorkflow(this)) withInput AddMemberToOrganizationInput(
                    organizationName = organizationName,
                    username = memberUsername,
                    firstname = memberFirstname,
                    lastname = memberLastname
                )

                // Verification: new member appears in member list
                val membersCard = OrganizationManagementPage(page).MembersCard()
                assertThat(membersCard.memberRow(memberUsername)).isVisible()


                NavBar(page).moveToDashboard()


                // val dashboard = DashboardPage(page)
                // dashboard.load()
                val auctionsCard = DashboardPage.AuctionsCard(page)
                assertThat(auctionsCard).isVisible()
                auctionsCard.click()

                val auctionName = "Test Auction - $suffix"
                val auctionDate = today().plusDays(10).atTime(0, 0)
                val targetAmount = 100_000.0
                val benchmark = 100.0
                val minimalBid = 0.0
                val solidarityContribution = 10.0

                val auction = run(CreateAuctionWorkflow(this)) withInput CreateAuctionInput(
                    "Test Auction",
                    auctionDate,
                    organizationName
                )

                // Now we have the auction. In order to set the type, we have to configure the auction
                // val configuredAuction =
                 run(UpdateAuctionFromAuctionsPageWorkflow(this)) withInput UpdateAuctionInput(
                    auction.id,
                    auctionName,
                    auctionDate,
                    organizationName,
                    AuctionDetails.SolawiTuebingen(
                        minimalBid,
                        benchmark,
                        targetAmount,
                        solidarityContribution
                    )
                )

                // Further assertions:
                // Deletion is enabled
                // Run auction is enabled
                // ...
                with(AuctionsPage(page).list()) {
                    val item = item(auction.id)
                    assertThat(item.updateButton()).isEnabled()
                    assertThat(item.deleteButton()).isEnabled()
                    assertThat(item.runAuctionButton()).isEnabled()
                }
            }
        }
    }
}
