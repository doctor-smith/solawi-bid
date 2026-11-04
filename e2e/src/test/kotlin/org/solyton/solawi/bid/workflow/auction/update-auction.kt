package org.solyton.solawi.bid.workflow.auction

import kotlinx.datetime.LocalDateTime
import org.evoleq.language.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.AuctionDetails
import org.solyton.solawi.bid.module.bid.data.api.ConfigureAuction
import org.solyton.solawi.bid.pageobjects.module.auction.AuctionsPage
import org.solyton.solawi.bid.pageobjects.module.auction.modal.UpdateAuctionModal

data class UpdateAuctionInput(
    val id: String,
    val name: String,
    // At the moment, only the date part is used
    val date: LocalDateTime,
    // Path to the host organization (root/to/org)
    val nameOfHostOrganization: String,
    // At the moment there is only one auction type available.
    // This shall change in the future
    val auctionDetails: AuctionDetails
)

/**
 * Assumes that the user already navigated to the auctions-page
 */
class UpdateAuctionFromAuctionsPageWorkflow(
    override val context: TestUserContext
): TestWorkflow<UpdateAuctionInput, ApiAuction>{
    override fun TestUserContext.runWith(input: UpdateAuctionInput): ApiAuction{
        // Choose the auction to update from the auction list
        with(AuctionsPage(page).list()) {
            val item = item(input.id)
            assertThat(item).isVisible()
            val button = item.updateButton()
            assertThat(button).isVisible()

            button.click()

            val modal = UpdateAuctionModal(
                page.getByDataId("update-auction.modal"),
                button
            )

            modal.nameInput().fill(input.name)
            modal.dateInput().fill(input.date.format(Locale.Iso))
            modal.organizationDropdown(setOf(input.nameOfHostOrganization))
                .select(input.nameOfHostOrganization)

            // now we need to distinguish cases by auction type
            when(val details = input.auctionDetails) {
                is AuctionDetails.SolawiTuebingen -> {
                    modal.targetAmountInput().fill(details.targetAmount.toString())
                    modal.benchmarkInput().fill(details.benchmark.toString())
                    modal.solidarityContributionInput().fill(details.solidarityContribution.toString())

                    if(modal.minimalBidInput().isVisible) {
                        modal.minimalBidInput().fill(details.minimalBid.toString())
                    }
                }
                is AuctionDetails.Empty -> {}
            }

            val auction = page.waitForApi(
                solawiApi,
                ConfigureAuction::class
            ) {
                modal.submit()
            }.decodeBodyTo<ApiAuction>()

            assertEquals(input.name, auction.name)
            assertEquals(input.date, auction.date)
            assertEquals(input.auctionDetails, auction.auctionDetails)



            assertThat(modal).not().isVisible()

            return auction
        }
    }
}
