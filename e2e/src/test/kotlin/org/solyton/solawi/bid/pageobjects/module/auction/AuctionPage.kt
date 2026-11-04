package org.solyton.solawi.bid.pageobjects.module.auction

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.solyton.solawi.bid.TestComponent
import org.solyton.solawi.bid.TestPage

class AuctionPage(private val page: Page): TestPage(page, "/app/auctions/:auctionId") {

    class Menu(override val locator: Locator) : TestComponent(locator)




}
