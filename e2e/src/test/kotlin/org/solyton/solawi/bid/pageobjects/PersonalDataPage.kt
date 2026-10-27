package org.solyton.solawi.bid.pageobjects

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfile
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.PhoneNumber

/**
 * Page object for the current user's personal-data page (`/app/private/data`), covering
 * creation of a personal user profile (name, contact data, address).
 */
class PersonalDataPage(private val page: Page) : TestPage(page, "app/private/data") {

    fun navigateTo(hasToNavigate: Boolean = false) {
        if (hasToNavigate) load()
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
    ): UserProfile {
        // assumes user to be on the personal data page
        page.clickOn("private-data.page.profile.chevron")
        page.clickOn("private-data.page.profile.create")

        val modal = page.getByDataId("upsert-user-profile.modal")
        assertThat(modal).isVisible()

        title?.let {
            page.getByDataId("user-profile.form.title-input").fill(title)
        }
        page.getByDataId("user-profile.form.firstname-input").fill(firstname.value)
        page.getByDataId("user-profile.form.lastname-input").fill(lastname.value)
        phoneNumber?.value?.let {
            page.getByDataId("user-profile.form.phone-number-input").fill(it)
        }
        mobileNumber?.value?.let {
            page.getByDataId("user-profile.form.cell-phone-number-input").fill(it)
        }

        // address data
        page.getByDataId("user-profile.form.address.recipient-name-input").fill(recipientName)
        organizationName?.let { page.getByDataId("user-profile.form.address.organization-name-input").fill(it) }
        page.getByDataId("user-profile.form.address.address-line-1-input").fill(addressLine1)
        addressLine2?.let {
            page.getByDataId("user-profile.form.address.address-line-2-input").fill(it)
        }
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

        val response = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateUserProfile::class
        ) {
            submitButton.click()
        }

        assertThat(modal).not().isVisible()

        val userProfile = response.decodeBodyTo<UserProfile>()
        return userProfile
    }
}
