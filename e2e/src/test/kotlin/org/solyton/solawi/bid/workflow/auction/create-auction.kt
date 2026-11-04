package org.solyton.solawi.bid.workflow.auction

import kotlinx.datetime.LocalDateTime
import org.evoleq.language.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.CreateAuction
import org.solyton.solawi.bid.pageobjects.module.auction.AuctionsPage

data class CreateAuctionInput(
    val name: String,
    // At the moment, only the date part is used
    val date: LocalDateTime,
    // Path to the host organization (root/to/org)
    val nameOfHostOrganization: String
)

class CreateAuctionWorkflow(
    override val context: TestUserContext
) : TestWorkflow<CreateAuctionInput, ApiAuction> {
    override fun TestUserContext.runWith(input: CreateAuctionInput): ApiAuction {
        // Navigate to the auctions page

        with(AuctionsPage(page)){

            val modal = CreateAuctionModal()
            modal.open()

            // Fill the form
            modal.nameInput().fill(input.name)
            modal.dateInput().fill(input.date.format(Locale.Iso))
            modal.organizationDropdown(setOf(input.nameOfHostOrganization))
                .select(input.nameOfHostOrganization)

            assertThat(modal.nameInput()).hasValue(input.name)
            assertThat(modal.dateInput()).hasValue(input.date.format(Locale.Iso))

            // hit the submit-button and wait
            // for the api response
            val auction = page.waitForApi(
                solawiApi,
                CreateAuction::class
            ) {
                modal.submit()
            }.decodeBodyTo<ApiAuction>()

            assertEquals(input.name, auction.name)
            assertEquals(input.date, auction.date)

            assertThat(modal).not().isVisible()

            val listOfAuctions = list()
            assertThat(listOfAuctions.item(auction.id)).isVisible()

            return auction
        }
    }
}