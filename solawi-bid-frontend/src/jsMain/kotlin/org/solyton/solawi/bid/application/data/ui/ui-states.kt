package org.solyton.solawi.bid.application.data.ui

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.application.ui.page.user.data.UiStateOfOrganizationPage

@Lensify
data class UiStates(
    @ReadWrite val ofOrganizationPage: UiStateOfOrganizationPage = UiStateOfOrganizationPage()
)
