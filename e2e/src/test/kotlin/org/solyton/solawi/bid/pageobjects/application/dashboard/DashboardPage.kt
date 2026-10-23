package org.solyton.solawi.bid.pageobjects.application.dashboard

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import getByDataId
import org.solyton.solawi.bid.TestComponent
import org.solyton.solawi.bid.TestLink
import org.solyton.solawi.bid.TestPage

class DashboardPage(
    private val page: Page,
    private val url: String = "app/dashboard",
) : TestPage(page, url)  {

    abstract class Card(override val locator: Locator) : TestComponent(locator)

    class PrivateDataCard(
        val page : Page
    ) : Card(
        locator = page.getByDataId("page.dashboard.card.personal-data")
    ) {

    }

    class OrganizationCard(
        val page : Page
    ) : Card(
        locator = page.getByDataId("page.dashboard.card.organization-management")
    ) {

    }

    class ApplicationManagementCard(
        val page: Page
    ) : Card(
        locator = page.getByDataId("page.dashboard.card.application-management")
    ) {
        fun userManagementLink(): TestLink =
            TestLink(page.getByDataId("page.dashboard.application-management.user-management"))
    }

    class SharesCard(
        val page: Page
    ) : Card(
        locator = page.getByDataId("page.dashboard.card.shares")
    )

    fun privateDataCard(): PrivateDataCard =
        PrivateDataCard(page)

    fun applicationManagementCard(): ApplicationManagementCard =
        ApplicationManagementCard(page)

    fun sharesCard(): SharesCard =
        SharesCard(page)

    fun organizationsCard(): OrganizationCard =
        OrganizationCard(page)

}