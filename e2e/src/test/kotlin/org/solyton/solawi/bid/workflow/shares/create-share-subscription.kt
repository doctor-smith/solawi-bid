package org.solyton.solawi.bid.workflow.shares

import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscription
import org.solyton.solawi.bid.pageobjects.SharesPage
import org.solyton.solawi.bid.waitForLoadingToFinish

data class CreateShareSubscriptionInput(
    val memberName: String,
    val fiscalYearKey: String,
    val shareTypeName: String,
    val distributionPointName: String,
    val price: String = "100",
    val quantity: String = "1",
    val ahcAuthorized: Boolean = false,
    val attachSepaMandate: Boolean = false
)

class CreateShareSubscriptionWorkflow(
    override val context: TestUserContext
) : TestWorkflow<CreateShareSubscriptionInput, ApiShareSubscription> {
    override fun TestUserContext.runWith(input: CreateShareSubscriptionInput): ApiShareSubscription {
        val sharesPage = SharesPage(page)
        val subscription = sharesPage.createShareSubscription(
            user.username.value,
            input.memberName,
            input.fiscalYearKey,
            input.shareTypeName,
            input.distributionPointName,
            input.price,
            input.quantity,
            input.ahcAuthorized
        )
        waitForLoadingToFinish()

        if (input.attachSepaMandate) {
            sharesPage.attachSepaMandateToSubscription()
            waitForLoadingToFinish()
        }

        return subscription
    }
}
