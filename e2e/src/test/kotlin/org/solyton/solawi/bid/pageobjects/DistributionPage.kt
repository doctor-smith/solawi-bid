package org.solyton.solawi.bid.pageobjects

import clickOn
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import getByDataId

/**
 * Page object for the Distribution application's organization page
 * (`/app/management/organizations/{organizationId}/distribution`).
 */
class DistributionPage(private val page: Page) {

    /** Creates a distribution point with the given [name] (its only field). */
    fun createDistributionPoint(name: String) {
        page.clickOn("distribution.page.distribution-point.create-button")

        val modal = page.getByDataId("distribution.upsert-distribution-point.modal")
        assertThat(modal).isVisible()

        page.locator("#distribution-point-name").fill(name)
        page.clickOn("distribution.upsert-distribution-point.modal.submit-button")

        assertThat(modal).not().isVisible()
    }
}
