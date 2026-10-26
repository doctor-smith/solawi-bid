package org.solyton.solawi.bid.pageobjects

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.clickOn
import org.solyton.solawi.bid.decodeBodyTo
import org.solyton.solawi.bid.getByDataId
import org.solyton.solawi.bid.module.distribution.data.api.ApiDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.CreateDistributionPoint
import org.solyton.solawi.bid.waitForApi

/**
 * Page object for the Distribution application's organization page
 * (`/app/management/organizations/{organizationId}/distribution`).
 */
class DistributionPage(private val page: Page) {

    /** Creates a distribution point with the given [name] (its only field). */
    fun createDistributionPoint(name: String): ApiDistributionPoint {
        page.clickOn("distribution.page.distribution-point.create-button")

        val modal = page.getByDataId("distribution.upsert-distribution-point.modal")
        assertThat(modal).isVisible()

        page.locator("#distribution-point-name").fill(name)

        val distributionPoint = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateDistributionPoint::class
        ) {
            page.clickOn("distribution.upsert-distribution-point.modal.submit-button")
        }.decodeBodyTo<ApiDistributionPoint>()


        assertThat(modal).not().isVisible()

        return distributionPoint
    }
}
