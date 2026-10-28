package org.solyton.solawi.bid.workflow.shares

import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.module.shares.data.api.ApiShareOffer
import org.solyton.solawi.bid.navigateToOrganizationApplicationPage
import org.solyton.solawi.bid.pageobjects.SharesPage
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar
import org.solyton.solawi.bid.waitForLoadingToFinish

data class SepaCollectionConfig(
    val collectionKey: String,
    val mandateReferencePrefix: String,
    val creditorAccountDescription: String = "e2e-creditor-account",
    val remittanceInformation: String = "E2E fixture collection"
)

data class ShareOfferConfig(
    val fiscalYearKey: String,
    val price: String = "100",
    val ahcAuthorizationRequired: Boolean = false,
    val sepaCollectionConfig: SepaCollectionConfig? = null
)

data class SetupSharesInput(
    val organizationName: String,
    val shareTypeName: String,
    val shareTypeKey: String,
    val shareTypeDescription: String = "E2E fixture share type",
    val offers: List<ShareOfferConfig>
)

class SetupSharesWorkflow(
    override val context: TestUserContext
) : TestWorkflow<SetupSharesInput, List<ApiShareOffer>> {
    override fun TestUserContext.runWith(input: SetupSharesInput): List<ApiShareOffer> {
        val navBar = NavBar(page)
        val dashboardPage = DashboardPage(page)
        val sharesPage = SharesPage(page)

        navBar.moveToDashboard()
        dashboardPage.shareManagementOrganizationManagementLink().click()
        page.navigateToOrganizationApplicationPage("shares", input.organizationName, false)

        sharesPage.createShareType(input.shareTypeName, input.shareTypeKey, input.shareTypeDescription)

        return input.offers.map { offerConfig ->
            val offer = sharesPage.createShareOffer(
                offerConfig.fiscalYearKey,
                input.shareTypeName,
                offerConfig.price,
                offerConfig.ahcAuthorizationRequired
            )
            waitForLoadingToFinish()

            offerConfig.sepaCollectionConfig?.let { sepa ->
                sharesPage.attachSepaCollectionToOffer(
                    shareOffer = offer,
                    creditorAccountHolder = input.organizationName,
                    creditorAccountDescription = sepa.creditorAccountDescription,
                    collectionKey = sepa.collectionKey,
                    mandateReferencePrefix = sepa.mandateReferencePrefix,
                    remittanceInformation = sepa.remittanceInformation
                )
                waitForLoadingToFinish()
            }
            offer
        }
    }
}
