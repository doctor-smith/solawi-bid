package org.solyton.solawi.bid.pageobjects.module.auction.modal

import com.microsoft.playwright.Locator
import org.solyton.solawi.bid.TestDropdown
import org.solyton.solawi.bid.TestModal
import org.solyton.solawi.bid.getByDataId
import org.solyton.solawi.bid.pageobjects.module.user.dropdown.OrganizationDropdown

class UpdateAuctionModal(
    override val locator: Locator,
    override val trigger: Locator
) :     TestModal(
    locator,
    trigger
) {
    companion object {
        const val BASE_ID = "update-auction"
    }

    override fun cancelButton(): Locator =
        locator.getByDataId("$BASE_ID.modal.cancel-button")

    override fun closeButton(): Locator =
        locator.getByDataId("$BASE_ID.modal.close-button")

    override fun submitButton(): Locator =
        locator.getByDataId("$BASE_ID.modal.submit-button")


    fun nameInput(): Locator = locator.getByDataId("$BASE_ID.form.input.name")

    fun dateInput(): Locator = locator.getByDataId("$BASE_ID.form.input.date")

    fun targetAmountInput(): Locator = locator.getByDataId("$BASE_ID.form.input.target-amount")

    fun benchmarkInput(): Locator = locator.getByDataId("$BASE_ID.form.input.benchmark")

    fun minimalBidInput(): Locator = locator.getByDataId("$BASE_ID.form.input.minimal-bid")

    fun solidarityContributionInput(): Locator = locator.getByDataId("$BASE_ID.form.input.solidarity-contribution")

    fun organizationDropdown(options: Set<String>): TestDropdown =
        OrganizationDropdown(
            locator.getByDataId("organization-dropdown.wrapper"),
            TestDropdown.options(locator, options)
        )
}