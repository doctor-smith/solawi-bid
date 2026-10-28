package org.solyton.solawi.bid.workflow.user

import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.assertThat
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.PhoneNumber
import org.solyton.solawi.bid.pageobjects.PersonalDataPage
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar
import org.solyton.solawi.bid.waitForLoadingToFinish

data class CreateProfileInput(
    val title: String = "Dr.",
    val firstname: Firstname = Firstname("Jane"),
    val lastname: Lastname = Lastname("Doe"),
    val phoneNumber: PhoneNumber = PhoneNumber("0123456789"),
    val mobileNumber: PhoneNumber = PhoneNumber("0123456789"),
    val recipientName: String = "${firstname.value} ${lastname.value}",
    val organizationName: String,
    val addressLine1: String = "123 Main Street",
    val addressLine2: String = "Apt 2",
    val city: String = "Anytown",
    val postalCode: String = "12345",
    val stateOrProvince: String = "DE-BW",
    val countryCode: String = "DE"
)

class CreateProfileWorkflow(
    override val context: TestUserContext
) : TestWorkflow<CreateProfileInput, Unit> {
    override fun TestUserContext.runWith(input: CreateProfileInput) {
        val navBar = NavBar(page)
        val dashboardPage = DashboardPage(page)
        val personalDataPage = PersonalDataPage(page)

        val personalDataCard = dashboardPage.privateDataCard()
        assertThat(personalDataCard).isVisible()
        personalDataCard.click()
        waitForLoadingToFinish()

        personalDataPage.createProfile(
            input.title,
            input.firstname,
            input.lastname,
            input.phoneNumber,
            input.mobileNumber,
            input.recipientName,
            input.organizationName,
            input.addressLine1,
            input.addressLine2,
            input.postalCode,
            input.city,
            input.countryCode,
            input.stateOrProvince
        )
        navBar.moveToDashboard()
    }
}
