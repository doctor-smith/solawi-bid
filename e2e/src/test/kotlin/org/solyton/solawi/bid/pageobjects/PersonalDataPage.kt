package org.solyton.solawi.bid.pageobjects

import TestConfig
import clickOn
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import dismissStrayErrorDialogIfPresent
import getByDataId
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.PhoneNumber
import waitForLoadingToFinish

/**
 * Page object for the current user's personal-data page (`/app/private/data`), covering
 * creation of a personal user profile (name, contact data, address).
 */
class PersonalDataPage(private val page: Page) {

    fun navigateTo(hasToNavigate: Boolean = false) {
        if (hasToNavigate) page.navigate("${TestConfig.BASE_URL}/app/private/data")
        page.dismissStrayErrorDialogIfPresent()
        page.waitForLoadingToFinish()
    }

    @Suppress("LongParameterList")
    fun createProfile(
        title: String?,
        firstname: Firstname,
        lastname: Lastname,
        phoneNumber: PhoneNumber?,
        mobileNumber: PhoneNumber?,
        recipientName: String,
        organizationName: String?,
        addressLine1: String,
        addressLine2: String?,
        postalCode: String,
        city: String,
        countryCode: String,
        stateOrProvince: String
    ) {
        // assumes user to be on the personal data page
        page.clickOn("private-data.page.profile.chevron")
        page.clickOn("private-data.page.profile.create")

        val modal = page.getByDataId("upsert-user-profile.modal")
        assertThat(modal).isVisible()

        page.getByDataId("user-profile.form.title-input").fill(title)
        page.getByDataId("user-profile.form.firstname-input").fill(firstname.value)
        page.getByDataId("user-profile.form.lastname-input").fill(lastname.value)
        page.getByDataId("user-profile.form.phone-number-input").fill(phoneNumber?.value)
        page.getByDataId("user-profile.form.cell-phone-number-input").fill(mobileNumber?.value)

        // address data
        page.getByDataId("user-profile.form.address.recipient-name-input").fill(recipientName)
        page.getByDataId("user-profile.form.address.organization-name-input").fill(organizationName)
        page.getByDataId("user-profile.form.address.address-line-1-input").fill(addressLine1)
        page.getByDataId("user-profile.form.address.address-line-2-input").fill(addressLine2)
        page.getByDataId("user-profile.form.address.city-input").fill(city)
        page.getByDataId("user-profile.form.address.postal-code-input").fill(postalCode)

        /*
        Need solution:
        Needs to click on translation of country code and state or province to current user language

        page.clickOn("user-profile.form.address.country-code-input")
        page.clickOn("user-profile.form.address.country-code-input.option.$countryCode")
        page.clickOn("user-profile.form.address.state-or-province-input")
        page.clickOn("user-profile.form.address.state-or-province-input.option.$stateOrProvince")
         */

        val submitButton = page.getByDataId("upsert-user-profile.modal.submit-button")
        assertThat(submitButton).isEnabled()
        submitButton.click()

        assertThat(modal).not().isVisible()
    }
}
