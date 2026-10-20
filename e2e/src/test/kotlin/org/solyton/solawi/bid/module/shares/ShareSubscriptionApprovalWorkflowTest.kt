package org.solyton.solawi.bid.module.shares

import CHECK_FALSE
import TestConfig
import TestUtils
import checkIcon
import clickOn
import com.microsoft.playwright.*
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.WaitForSelectorState
import getByDataId
import inputText
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Develop
import org.solyton.solawi.bid.Workflow
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.PhoneNumber
import put
import shot
import waitForLoadingToFinish
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * End-to-end coverage for the Share Management module, workflow B1 from
 * `.junie/progress/share-management-e2e-progress.md`:
 *
 *   PendingActivation --(PROVIDER, NO_PAYMENT_MANDATE_REQUIRED)--> Subscribed
 *
 * Unlike every other e2e test in this project, Share Management has no pre-seeded
 * organization to build on: `TEST_USER` has no organization with Banking, Distribution
 * or Share Management booked. This test therefore builds its own, fully isolated
 * fixture organization from scratch, purely by driving the admin UI (no direct
 * database/backend shortcuts), covering in order:
 *
 *   1. Organization creation (`/app/management/organizations`)
 *   2. Booking Banking, Distribution and Share Management for it
 *      (`/app/management/private`, "connect" flow)
 *   3. Creating a fiscal year (Banking application page)
 *   4. Creating a share type and a fiscal-year-scoped share offer (Share Management page)
 *   5. Creating a distribution point (Distribution application page)
 *   6. Creating a member subscription to the offer (Share Management page)
 *   7. Approving the subscription via the "Bulk Edit" status-change dialog
 *
 * The full click-by-click path (including every `data-id` used below, and the ones
 * that had to be added to production UI code to make this possible) is documented in
 * `.junie/progress/share-management-e2e-progress.md` and
 * `docs/e2e-fixture-workflows.md` for reuse by this and other modules' e2e tests.
 */
class ShareSubscriptionApprovalWorkflowTest {

    companion object {
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
            // Logs in once as TEST_USER (who becomes the owner/admin of every organization
            // it creates, see `createOrganizationViaAdminUi`) and stores the resulting
            // session, so the test can reuse it.
            TestUtils().getCookie(browser)
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            browser.close()
            playwright.close()
            TestUtils().deleteStorageStateFile()
        }
    }

    // A fresh context, already logged in as TEST_USER via the stored session.
    private fun adminContext(): BrowserContext {
        val storedState = Files.readString(TestUtils().storageStatePath)
        return browser.newContext(
            Browser.NewContextOptions().setStorageState(storedState)
        )
    }

    /**
     * Navigating straight to one of these pages can briefly flash one or more unrelated
     * error dialogs on first mount (e.g. a "coroutine scope left the composition" error, or
     * the organization page's per-row application-context lookups transiently failing right
     * after a context was just created), which cover the whole page and can even stack on
     * top of each other. If any show up, dismiss all of them so the page below becomes
     * interactable again, just like a real user clicking them away one by one would.
     */
    private fun dismissStrayErrorDialogIfPresent(page: Page) {
        val errorOk = page.getByDataId("null.modal.submit-button")
        while (true) {
            try {
                errorOk.first().waitFor(
                    Locator.WaitForOptions().setTimeout(3000.0).setState(WaitForSelectorState.VISIBLE)
                )
                errorOk.first().click()
            } catch (e: TimeoutError) {
                // No (more) stray error dialogs - nothing left to dismiss.
                return
            }
        }
    }

    /**
     * Creates a brand-new root organization with a unique [name] via
     * `/app/management/organizations`. TEST_USER (the creator) automatically becomes a
     * member of it (`createRootOrganization` inserts the creator into `UserOrganization`),
     * so it can later subscribe to a share offer in this same organization without any
     * separate "add member" step.
     */
    private fun createOrganizationViaAdminUi(page: Page, name: String, hasToNavigate: Boolean = true) {
        if(hasToNavigate)  page.navigate("${TestConfig.BASE_URL}/app/management/organizations")
        dismissStrayErrorDialogIfPresent(page)
        page.waitForLoadingToFinish()

        page.clickOn("organization-management-page.button.create-organization")
        page.locator("#organization-name").fill(name)
        page.clickOn("null.modal.submit-button")

        page.waitForSelector("text=$name")
    }

    /**
     * Clicks [dataId] if (and only if) it is currently visible, within a short timeout.
     * Used to drive an application through whichever lifecycle steps it still needs
     * (available -> registered -> trialing -> active), without needing to know its
     * current state up front: steps that don't apply (already done, or not reached yet)
     * are simply skipped.
     */
    private fun clickIfPresent(page: Page, dataId: String, timeoutMs: Double = 3000.0) {
        val locator = page.getByDataId(dataId).first()
        try {
            locator.waitFor(Locator.WaitForOptions().setTimeout(timeoutMs).setState(WaitForSelectorState.VISIBLE))
        } catch (e: TimeoutError) {
            return
        }
        locator.click()
        page.waitForLoadingToFinish()
    }

    /**
     * Ensures [applicationName] is personally `Active` for the current user (TEST_USER),
     * which is a prerequisite for the "connect to organization" button to even render on
     * `/app/management/private`. Walks through whichever of the lifecycle steps
     * (register -> start trial -> subscribe) still apply; already-completed or
     * not-yet-reached steps are harmlessly skipped by [clickIfPresent].
     */
    private fun ensureApplicationActiveForTestUser(page: Page, applicationName: String, hasToNavigate: Boolean = true) {
        if(hasToNavigate) {
            page.navigate("${TestConfig.BASE_URL}/app/management/private")
            dismissStrayErrorDialogIfPresent(page)
            page.waitForLoadingToFinish()
        }
        clickIfPresent(page, "private-application-management.page.available-application.$applicationName.register-button")
        // page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("dev/0001.png")))
        clickIfPresent(page, "private-application-management.page.application.$applicationName.start-trial-button")
        // page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("dev/0002.png")))
        clickIfPresent(page, "private-application-management.page.application.$applicationName.subscribe-button")
        // page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("dev/0003.png")))
        page.getByDataId("private-application-management.page.application.$applicationName.connect-button")
            .first()
            .waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE), )

        // page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("dev/0004.png")))
    }

    /**
     * Books [applicationName] (e.g. "BANKING", "DISTRIBUTION", "SHARE_MANAGEMENT") for the
     * organization called [organizationName] via the private application-management
     * "connect" flow (`/app/management/private`). Ensures the application is personally
     * `Active` for the current user first, since the connect button is only rendered then.
     */
    private fun bookApplicationViaAdminUi(page: Page, applicationName: String, organizationName: String, hasToNavigate: Boolean = true) {
        ensureApplicationActiveForTestUser(page, applicationName, hasToNavigate)

        page.clickOn("private-application-management.page.application.$applicationName.connect-button")
        page.clickOn("connect-application-to-organization.modal.organization-dropdown")
        page.clickOn("connect-application-to-organization.modal.organization-dropdown.option.$organizationName")
        page.clickOn("null.modal.submit-button")

        // Confirm the booking actually persisted before moving on: the organization name
        // shows up in the "linked organizations" column of the application's row.
        page.waitForSelector("text=$organizationName")
    }

    /**
     * Grants the current user (TEST_USER) every available role on this
     * application-organization context, via the organization's own "connected
     * applications" card -> "manage permissions" dialog. Plain `OWNER` (auto-granted server
     * side at booking time, see `connectApplicationToOrganization`) alone is not sufficient
     * for every action (e.g. creating a legal entity), so every role checkbox is checked here;
     * the user will restrict this later once the exact required roles are known.
     */
    private fun grantOwnerPermissionViaAdminUi(page: Page, organizationName: String, applicationName: String, username: String, hasToNavigate: Boolean = true) {
        if(hasToNavigate) page.navigate("${TestConfig.BASE_URL}/app/management/organizations")
        dismissStrayErrorDialogIfPresent(page)
        page.waitForLoadingToFinish()

        page.clickOn("organization-management-page.organization-list.organization.$organizationName.details-button")
        page.waitForLoadingToFinish()
        dismissStrayErrorDialogIfPresent(page)

        page.clickOn("organization.page.connected-applications.chevron")
        page.clickOn("organization.page.connected-applications.$applicationName.manage-permissions-button")
        page.waitForLoadingToFinish()
        dismissStrayErrorDialogIfPresent(page)

        page.clickOn("private-application-organization-management.page.member.$username.manage-permissions-button")

        // Check every available role checkbox (not just the already-checked, non-togglable
        // OWNER one), since OWNER alone is not always sufficient for every action.
        val roleCheckboxes = page.locator("input[type='checkbox'][data-id^='manage-user-permissions.modal.role.']")
            // page.locator("[data-id^='manage-user-permissions.modal.role.'][data-id$='.checkbox']")
        roleCheckboxes.first().waitFor()
        val count = roleCheckboxes.count()
        if(count <= 0) throw IllegalArgumentException("There should be at lease one role")
        // roleCheckboxes.screenshot(Locator.ScreenshotOptions().setPath(Paths.get("dev/role-checkboxes-$applicationName.png")))
        for (i in 0 until count) {
            val checkbox = roleCheckboxes.nth(i)
            if(checkbox.isChecked.not()) checkbox.check()
        }
        // page.shot("dev/checked-roles-$applicationName.png")
        page.clickOn("null.modal.submit-button")
    }

    /**
     * Navigates to the organization-scoped page of a booked application by going through
     * its "organizations with <application>" list page and clicking the plain-text link
     * for [organizationName], rather than guessing the organization's id.
     */
    private fun navigateToOrganizationApplicationPage(page: Page, listSegment: String, organizationName: String, hasToNavigate: Boolean = false) {
        if(hasToNavigate) page.navigate("${TestConfig.BASE_URL}/app/management/organizations/with/$listSegment")
        dismissStrayErrorDialogIfPresent(page)
        page.waitForLoadingToFinish()

        // This list page renders each organization as a plain clickable `<span>` (the
        // shared `Link` composable, which does client-side routing via `onClick`, not a
        // real `<a>` tag) with the organization's name as its text (no `data-id`), so
        // locate it by text instead.
        page.locator("span", Page.LocatorOptions().setHasText(organizationName)).first().click()
        page.waitForLoadingToFinish()
        dismissStrayErrorDialogIfPresent(page)
    }

    private fun navigateToPersonalDataPage(page: Page, hasToNavigate: Boolean = false) {
        if(hasToNavigate) page.navigate("${TestConfig.BASE_URL}/app/private/data")
        dismissStrayErrorDialogIfPresent(page)
        page.waitForLoadingToFinish()
    }

    /**
     * Creates a legal entity (type HUMAN) for the organization and, once it exists, attaches a
     * creditor identifier (the "Gl\u00e4ubiger-ID") to it - a two-step UI flow, since the
     * "create" modal only sets up the legal entity itself, while the creditor id can only be
     * set afterward via "edit". Both the legal entity and a creditor id are required before
     * SEPA-related Banking operations (e.g. fiscal years) are permitted for the organization.
     */
    private fun createLegalEntityWithCreditorIdViaAdminUi(page: Page, name: String, legalForm: String, creditorId: String) {
        page.evaluate("console.log('MARKER_BEFORE_CREATE_LEGAL_ENTITY_CLICK')")
        // page.shot("dev/create-legal-entity.png")
        page.clickOn("banking.page.legal-entity.create-button")
        page.evaluate("console.log('MARKER_AFTER_CREATE_LEGAL_ENTITY_CLICK')")

        // basic data
        page.locator("#name").fill(name)
        page.locator("#legal-form").fill(legalForm)
        page.clickOn("legal-entity-form.legal-entity-type-dropdown")
        // The dropdown's options are keyed by their displayed German label, not the enum
        // value (e.g. "Nat\u00fcrliche Person" -> LegalEntityType.HUMAN).
        page.clickOn("legal-entity-form.legal-entity-type-dropdown.option.Nat\u00fcrliche Person")

        // Deliberately NOT setting the creditor id here: the "create" dialog's onOk handler
        // only dispatches legal-entity creation, not creditor-identifier creation (and doing
        // so here would use a not-yet-known legal entity id). The real app flow - confirmed by
        // the user - is to create the legal entity first, then set the creditor id afterward
        // via "edit", which is what the second step below does.

        // address
        page.locator("#recipientName").fill("Recipient-$name")
        page.locator("#organizationName").fill(name)
        page.locator("#addressLine1").fill("line 1")
        page.locator("#addressLine2").fill("line 2")
        page.locator("#postalCode").fill("12345")

        page.inputText("#city", "city")


        // page.shot("dev/create-legal-entity-filled.png")

        page.clickOn("banking.modal.upsert-legal-entity.modal.submit-button")

        // page.shot("dev/created-legal-entity.png")

        page.waitForLoadingToFinish()

        page.waitForSelector("text=$name")

        // Step 2: now that the legal entity exists, re-open the form via the "edit" button
        // and set the creditor id there, matching the only supported production flow.
        page.clickOn("banking.page.legal-entity.edit-button")

        page.locator("#creditor-id").fill(creditorId)

        page.clickOn("banking.modal.upsert-legal-entity.modal.submit-button")

        page.waitForLoadingToFinish()

        page.waitForSelector("text=$creditorId")

    }

    /** Creates a fiscal year running from [start] to [end] (format `YYYY-MM-DD`). */
    private fun createFiscalYearViaAdminUi(page: Page, start: String, end: String) {
        page.clickOn("banking.page.fiscal-years.chevron")
        page.clickOn("banking.page.fiscal-years.create-button")

        val modal = page.getByDataId("banking.upsert-fiscal-years.modal")
        assertThat(modal).isVisible()


        val startDate = page.getByDataId("upsert-fiscal-year.form.input.date.start")
        val endDate = page.getByDataId("upsert-fiscal-year.form.input.date.end")
        val submitButton = page.getByDataId("banking.upsert-fiscal-years.modal.submit-button")

        startDate.apply {
            fill(start)
            assertThat(this).hasValue(start)
        }

        endDate.apply {
            fill(end)
            assertThat(this).hasValue(end)
        }


        submitButton.apply {
            assertThat(this@apply).isEnabled()
            click()
        }

        assertThat(modal).not().isVisible()

        // The fiscal year list shows the formatted year (e.g. "2026" when start/end share
        // the same year), confirming the creation was persisted.
        // val formattedYear = start.substring(0, 4)
        // page.waitForSelector("text=$formattedYear")

    }

    /**
     * Creates a share type with the given [name]/[key]/[description].
     * The key must have length <= 20
     */
    private fun createShareTypeViaAdminUi(page: Page, name: String, key: String, description: String) {
        page.clickOn("shares.page.share-type.chevron")
        page.clickOn("shares.page.share-type.create-button")

        val modal = page.getByDataId("shares.modal.upsert-share-type.modal")
        assertThat(modal).isVisible()

        page.put("shares.modal.upsert-share-type.name-input", name)
        page.put("shares.modal.upsert-share-type.key-input", key)
        page.put("shares.modal.upsert-share-type.description-input", description)

        page.shot("dev/share-type-filled")

        page.clickOn("shares.modal.upsert-share-type.modal.submit-button")
        assertThat(modal).not().isVisible()


       //  page.waitForSelector("text=$name")

    }

    /**
     * Creates a fixed-price share offer for [shareTypeName] on the fiscal year named
     * [fiscalYearKey] (its `FiscalYear.format()`, e.g. "2026"), with the given [price] and
     * SEPA mandate not required (keeps this fixture independent of any SEPA setup).
     */
    private fun createShareOfferViaAdminUi(page: Page, fiscalYearKey: String, shareTypeName: String, price: String) {
        page.clickOn("shares.page.share-offer.chevron")
        page.clickOn("shares.page.share-offer.create-button")

        val modal = page.getByDataId("shares.modal.upsert-share-offer.modal")
        assertThat(modal).isVisible()

        page.clickOn("shares.modal.upsert-share-offer.fiscal-year-dropdown")
        page.clickOn("shares.modal.upsert-share-offer.fiscal-year-dropdown.option.$fiscalYearKey")

        page.clickOn("shares.modal.upsert-share-offer.share-type-dropdown")
        page.clickOn("shares.modal.upsert-share-offer.share-type-dropdown.option.$shareTypeName")

        page.clickOn("shares.modal.upsert-share-offer.pricing-type-dropdown")
        page.clickOn("shares.modal.upsert-share-offer.pricing-type-dropdown.option.FIXED")

        page.put("shares.modal.upsert-share-offer.price-input", price)

        page.clickOn("shares.modal.upsert-share-offer.ahc-authorization-required-dropdown")
        page.clickOn("shares.modal.upsert-share-offer.ahc-authorization-required-dropdown.option.\u274C")

        page.clickOn("shares.modal.upsert-share-offer.modal.submit-button")

        assertThat(modal).not().isVisible()

        // page.waitForSelector("text=$shareTypeName")
    }

    /** Creates a distribution point with the given [name] (its only field). */
    private fun createDistributionPointViaAdminUi(page: Page, name: String) {
        page.clickOn("distribution.page.distribution-point.create-button")

        val modal = page.getByDataId("distribution.upsert-distribution-point.modal")
        assertThat(modal).isVisible()

        page.locator("#distribution-point-name").fill(name)
        page.clickOn("distribution.upsert-distribution-point.modal.submit-button")

        assertThat(modal).not().isVisible()
        // page.waitForSelector("text=$name")
    }

    private fun createPersonalUserProfile(
        page: Page,
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
        // assumes user to be on the personal data paged
        page.clickOn("private-data.page.profile.chevron")
        page.clickOn( "private-data.page.profile.create")

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

    /**
     * Creates a subscription for [username] to the share offer keyed by
     * `"$shareTypeName - $fiscalYearKey"`, at the given [distributionPointName], with
     * [price] and [numberOfShares]. The fiscal year, SEPA-authorization and initial status
     * (PendingActivation) are left at their defaults.
     */
    private fun createShareSubscriptionViaAdminUi(
        page: Page,
        username: String,
        fullName: String,
        fiscalYearKey: String,
        shareTypeName: String,
        distributionPointName: String,
        price: String,
        numberOfShares: String,
        ahcAuthorized: Boolean,
        coSubscribers: List<String> = emptyList()
    ) {
        page.clickOn("shares.page.share-subscription.chevron")
        page.clickOn("shares.page.share-subscription.create-button")

        val modal = page.getByDataId("shares.modal.upsert-share-subscription.modal")
        assertThat(modal).isVisible()

        val selectedUser = page.getByDataId("shares.modal.upsert-share-subscription.user-display-input")
        val userSelectionButton = page.getByDataId("shares.modal.upsert-share-subscription.user.$username.select-button")
        userSelectionButton.apply {
            assertThat(this).isVisible()
            assertThat(this).isEnabled()
            click()
        }
        assertThat(selectedUser).hasValue(fullName)


        page.clickOn("shares.modal.upsert-share-subscription.fiscal-year-dropdown")
        page.clickOn("shares.modal.upsert-share-subscription.fiscal-year-dropdown.option.$fiscalYearKey")

        page.clickOn("shares.modal.upsert-share-subscription.distribution-point-dropdown")
        page.clickOn("shares.modal.upsert-share-subscription.distribution-point-dropdown.option.$distributionPointName")

        page.clickOn("shares.modal.upsert-share-subscription.share-offer-dropdown")
        page.clickOn("shares.modal.upsert-share-subscription.share-offer-dropdown.option.$shareTypeName - $fiscalYearKey")

        page.put("shares.modal.upsert-share-subscription.price-per-share-input", price)
        assertThat(
            page.getByDataId("shares.modal.upsert-share-subscription.price-per-share-input")
        ).hasValue(price)

        page.put("shares.modal.upsert-share-subscription.number-of-shares-input", numberOfShares)
        assertThat(
            page.getByDataId("shares.modal.upsert-share-subscription.number-of-shares-input")
        ).hasValue(numberOfShares)

        page.clickOn("shares.modal.upsert-share-subscription.ahc-authorized-dropdown")
        page.clickOn("shares.modal.upsert-share-subscription.ahc-authorized-dropdown.option.${ahcAuthorized.checkIcon()}")

        page.put("shares.modal.upsert-share-subscription.co-subscribers-textarea", coSubscribers.joinToString("\n") { it })

        val submitButton = page.getByDataId("shares.modal.upsert-share-subscription.modal.submit-button")

        // validate before submit




        submitButton.apply {
            assertThat(this).isEnabled()
            click()
        }
        assertThat(modal).not().isVisible()


       //  page.waitForSelector("text=$distributionPointName")

    }

    /**
     * Approves the (single, currently selected-by-default-none) subscription via the
     * "Bulk Edit" dialog, changing its status to [targetStatus] (e.g. "SUBSCRIBED"). Only
     * safe for PROVIDER-permitted transitions: the dialog hardcodes `ChangedBy.PROVIDER`
     * and will throw a client-side error for USER-only transitions (see progress doc).
     */
    private fun changeStatusViaBulkEdit(page: Page, targetStatus: String) {
        page.clickOn("shares.page.share-subscription.select-all-checkbox")
        page.clickOn("shares.page.share-subscription.bulk-edit-button")

        page.clickOn("shares.modal.bulk-edit.change-status-checkbox")
        page.clickOn("shares.modal.bulk-edit.status-dropdown")
        page.clickOn("shares.modal.bulk-edit.status-dropdown.option.$targetStatus")


        val submitButton = page.getByDataId("shares.modal.bulk-edit.modal.submit-button")
        submitButton.apply {
            assertThat(this).isEnabled()
            click()
        }

        page.shot("dev/confirm-bulk-edit-share-subscriptions.png")
        // A second, generic confirmation dialog ("Are you sure...?") is the one that
        // actually dispatches the status change.
        page.clickOn("null.modal.submit-button")
    }

    /**
     * On any failure, dumps a full-page screenshot and the current page's HTML into
     * `build/reports/tests/screenshots/` for debugging, before rethrowing.
     */
    private fun <T> withFailureDiagnostics(page: Page, block: () -> T): T {
        val consoleLog = StringBuilder()
        val networkLog = StringBuilder()
        page.onConsoleMessage { msg -> consoleLog.appendLine("[console:${msg.type()}] ${msg.text()}") }
        page.onPageError { error -> consoleLog.appendLine("[pageerror] $error") }
        page.onResponse { response ->
            if (response.status() >= 400) {
                try {
                    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

                    networkLog.appendLine("=== ${response.request().method()} ${response.url()} -> ${response.status()} ===")
                    networkLog.appendLine("timestamp: $timestamp")
                    val context = response.request().headerValue("CONTEXT")
                    networkLog.appendLine("context-header: $context")
                    val postData = response.request().postData()
                    if (postData != null) networkLog.appendLine("request body: $postData")
                    networkLog.appendLine("response body: ${response.text()}")
                    // The page the test was on when this background request fired, so a failing
                    // call can be matched back to the fixture step that triggered it.
                    networkLog.appendLine("source-page-url: ${page.url()}")
                    networkLog.appendLine()
                } catch (ignored: Exception) {
                    // Response body may not be readable for every request type - ignore.
                }
            }
        }
        try {
            return block()
        } catch (e: Throwable) {
            try {
                val dir = java.nio.file.Paths.get("build/reports/tests/screenshots")
                Files.createDirectories(dir)
                page.screenshot(Page.ScreenshotOptions().setPath(dir.resolve("failure.png")).setFullPage(true))
                Files.writeString(dir.resolve("failure.html"), page.content())
                Files.writeString(dir.resolve("failure-console.log"), consoleLog.toString())
                Files.writeString(dir.resolve("failure-network.log"), networkLog.toString())
            } catch (diagnosticFailure: Exception) {
                // Best-effort diagnostics only - never hide the original failure.
            }
            throw e
        }
    }

    @Develop
    @Workflow@Test
    fun `provider approves pending subscription without payment mandate`() {
        val context = adminContext()
        try{
            val page = context.newPage()
            withFailureDiagnostics(page) {
                val suffix = System.currentTimeMillis()
                val organizationName = "e2e-shares-org-$suffix"
                val shareTypeName = "e2e-share-type-$suffix"
                val shareTypeKey = "key-$suffix"
                val distributionPointName = "e2e-dp-$suffix"
                val username = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
                val fiscalYearKey = "3026"
                val price = "100"

                // profile
                val title = "Dr."
                val firstname = Firstname("John")
                val lastname = Lastname("Doe")
                val phoneNumber = PhoneNumber("0123456789")
                val mobileNumber = PhoneNumber("0123456789")
                // address
                val recipientName = "John Doe"
                val addressLine1 = "123 Main Street"
                val addressLine2 = "Apt 1"
                val city = "Anytown"
                val postalCode = "12345"
                val stateOrProvince = "DE-BW"
                val countryCode = "DE"

                val moveToDashboard = {
                    page.getByDataId("navbar.apps-button").apply {
                        waitFor()
                        click()
                    }
                    page.waitForLoadingToFinish()
                }

                page.navigate("${TestConfig.BASE_URL}/app/dashboard")
                val personalDataCard = { page.getByDataId("page.dashboard.card.personal-data") }
                personalDataCard().waitFor()
                personalDataCard().click()
                navigateToPersonalDataPage(page, false)
                createPersonalUserProfile(
                    page,
                    title,
                    firstname,
                    lastname,
                    phoneNumber,
                    mobileNumber,
                    recipientName,
                    organizationName,
                    addressLine1,
                    addressLine2,
                    postalCode,
                    city,
                    countryCode,
                    stateOrProvince
                )

                moveToDashboard()



                // Create organization
                // 1. click on organization card
                val organizationCard = {page.getByDataId("page.dashboard.card.organization-management")}
                organizationCard().apply {
                    waitFor()
                    click()
                }

                dismissStrayErrorDialogIfPresent(page)
                page.waitForLoadingToFinish()

                createOrganizationViaAdminUi(page, organizationName)
                page.waitForLoadingToFinish()

                moveToDashboard()
                organizationCard().apply {
                    waitFor()
                }


                // 2. Book every application this fixture needs, then explicitly (re-)grant the
                // owner permission via the UI to refresh the client-side permission cache (see
                // `grantOwnerPermissionViaAdminUi`).
                val myAppsButton = page.getByDataId("page.dashboard.application-management.my-apps")
                myAppsButton.click()
                page.waitForLoadingToFinish()
                dismissStrayErrorDialogIfPresent(page)
                page.waitForLoadingToFinish()

                // page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("dev/$suffix/005_my-available-apps-$suffix.png")))

                bookApplicationViaAdminUi(page, "BANKING", organizationName, false)
                bookApplicationViaAdminUi(page, "DISTRIBUTION", organizationName,false)
                bookApplicationViaAdminUi(page, "SHARE_MANAGEMENT", organizationName, false)

                // page.shot("dev/$suffix/006_my-booked-apps-$suffix.png")

                grantOwnerPermissionViaAdminUi(page, organizationName, "BANKING", username)
                grantOwnerPermissionViaAdminUi(page, organizationName, "DISTRIBUTION", username)
                grantOwnerPermissionViaAdminUi(page, organizationName, "SHARE_MANAGEMENT", username)


                // back to dashboard
                moveToDashboard()
                val bankingButton = page.getByDataId("page.dashboard.banking-application.organization-management")
                bankingButton.apply {
                    waitFor()
                    click()
                }
                // 3. Legal entity + creditor id (required for SEPA-related Banking operations),
                // then the fiscal year itself (required before any share offer can be created).
                navigateToOrganizationApplicationPage(page, "banking", organizationName, false)
                // page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("dev/$suffix/007_organization-banking-$suffix.png")))

                // create Legal entity
                createLegalEntityWithCreditorIdViaAdminUi(page, organizationName, "e.V.", "DE98ZZZ09999999999")
                page.waitForLoadingToFinish()
                // page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("dev/$suffix/008_legal-entity-$suffix.png")))

                // create fiscal year
                createFiscalYearViaAdminUi(page, "$fiscalYearKey-01-01", "$fiscalYearKey-12-31")
                page.waitForLoadingToFinish()

                // page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("dev/$suffix/009_fiscal-years-$suffix.png")))

                // back to dashboard
                moveToDashboard()

                // Distribution point (required to create a subscription)
                val distributionManagementLink = page.getByDataId("page.dashboard.distribution-management.organization-management")
                distributionManagementLink.apply {
                    waitFor()
                    click()
                }

                navigateToOrganizationApplicationPage(page, "distribution", organizationName, false)
                createDistributionPointViaAdminUi(page, distributionPointName)
                page.waitForLoadingToFinish()

                moveToDashboard()

                // Share type + fiscal-year-scoped share offer.
                val shareManagementLink = page.getByDataId("page.dashboard.share-management.organization-management")
                shareManagementLink.apply {
                    waitFor()
                    click()
                }
                navigateToOrganizationApplicationPage(page, "shares", organizationName, false)
                createShareTypeViaAdminUi(page, shareTypeName, shareTypeKey, "E2E fixture share type")
                createShareOfferViaAdminUi(page, fiscalYearKey, shareTypeName, price)

                // Member subscription (TEST_USER is automatically a member of the org it
                // just created) - starts out as PendingActivation.
                // navigateToOrganizationApplicationPage(page, "shares", organizationName)
                createShareSubscriptionViaAdminUi(
                    page,
                    username,
                    "${firstname.value} ${lastname.value}",
                    fiscalYearKey,
                    shareTypeName,
                    distributionPointName,
                    price,
                    "1",
                    false,
                )

                page.waitForLoadingToFinish()

                // set status filter to PENDING_ACTIVATION
                val statusFilterDropdown = page.getByDataId("shares.page.share-subscription.status-dropdown")
                statusFilterDropdown.click()
                val pendingStatusOption = page.getByDataId("shares.page.share-subscription.status-dropdown.option.PENDING_ACTIVATION")
                pendingStatusOption.click()
                assertThat(pendingStatusOption).not().isVisible()

                // set ahc authorization filter to No
                val ahcAuthorizedFilterDropdown = page.getByDataId("shares.page.share-subscription.ahc-authorized-dropdown")
                ahcAuthorizedFilterDropdown.click()
                val ahcAuthorizedFilterNoOption = page.getByDataId("shares.page.share-subscription.ahc-authorized-dropdown.option.$CHECK_FALSE")
                ahcAuthorizedFilterNoOption.click()
                assertThat(ahcAuthorizedFilterNoOption).not().isVisible()

                val shareSubscriptionList = page.getByDataId("shares.page.share-subscription.list")
                val pendingRow = shareSubscriptionList.getByText("PENDING_ACTIVATION")
                //  page.waitForSelector("text=PENDING_ACTIVATION")
                Assertions.assertTrue(
                    pendingRow.isVisible,
                    "The newly created subscription should start out as PENDING_ACTIVATION."
                )

                // Provider approval without a payment mandate: PendingActivation ->
                // (PROVIDER, NO_PAYMENT_MANDATE_REQUIRED) -> Subscribed.
                changeStatusViaBulkEdit(page, "SUBSCRIBED")
                page.waitForLoadingToFinish()

                statusFilterDropdown.click()
                val subscribedStatusOption = page.getByDataId("shares.page.share-subscription.status-dropdown.option.SUBSCRIBED")
                subscribedStatusOption.click()
                assertThat(subscribedStatusOption).not().isVisible()

                val subscribedRow = shareSubscriptionList.getByText("SUBSCRIBED")
                Assertions.assertTrue(
                    subscribedRow.isVisible,
                    "The subscription should become SUBSCRIBED after provider approval without a payment mandate."
                )
            }
        }
        finally {
            context.close()
        }
    }
}
