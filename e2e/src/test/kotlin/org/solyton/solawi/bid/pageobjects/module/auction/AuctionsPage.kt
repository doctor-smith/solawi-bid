package org.solyton.solawi.bid.pageobjects.module.auction

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.solyton.solawi.bid.TestComponent
import org.solyton.solawi.bid.TestPage
import org.solyton.solawi.bid.getByDataId
import org.solyton.solawi.bid.pageobjects.module.auction.modal.CreateAuctionModal

class AuctionsPage(private val page: Page): TestPage(page, "/app/auctions") {
    companion object {
        const val BASE_ID = "auctions-page"
    }

    class ListOf(override val locator: Locator) : TestComponent(locator) {

        fun item(auctionId: String): Locator = locator.getByDataId("$BASE_ID.list.item.$auctionId")

        // run auction
        fun Locator.runAuctionButton(): Locator = getByDataId("run-auction-button")
        // update
        fun Locator.updateButton(): Locator = getByDataId("edit-button")
        // delete
        fun Locator.deleteButton(): Locator = getByDataId("delete-button")

    }

    fun list(): ListOf = ListOf(page.getByDataId("$BASE_ID.list"))

    fun CreateAuctionModal(): CreateAuctionModal = CreateAuctionModal(
        page.getByDataId("create-auction.modal"),
        page.getByDataId("$BASE_ID.create-auction-button")
    )
}
