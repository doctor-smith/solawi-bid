package org.solyton.solawi.bid.workflow.shares

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.getByDataId

data class TransferSubscriptionOwnershipInput(
    val subscriptionId: String,
    val newOwnerUsername: String
)

class TransferSubscriptionOwnershipWorkflow(
    override val context: TestUserContext
) : TestWorkflow<TransferSubscriptionOwnershipInput, Locator> {

    override fun TestUserContext.runWith(input: TransferSubscriptionOwnershipInput): Locator {
        val transferButton = page.getByDataId("shares.page.share-subscription.${input.subscriptionId}.transfer-ownership-button")
        transferButton.click()

        val modal = page.getByDataId("shares.modal.transfer-ownership.modal")
        assertThat(modal).isVisible()

        // Benutzer im Modal suchen und auswählen
        val searchInput = modal.locator("[data-id='shares.modal.transfer-ownership.user-search-input']")
        if (searchInput.isVisible) {
            searchInput.fill(input.newOwnerUsername)
        }

        // Select-Button für den neuen Benutzer anklicken
        val selectButton = modal.locator("[data-id='shares.modal.transfer-ownership.user.${input.newOwnerUsername}.select-button']")
            .or(modal.locator("[data-id*='select-button']"))
            .first()
        selectButton.click()

        // Bestätigen
        val confirmButton = modal.locator("[data-id='shares.modal.transfer-ownership.modal.submit-button']")
        confirmButton.click()

        assertThat(modal).isHidden()

        return page.getByDataId("shares.page.share-subscription.list")
    }
}