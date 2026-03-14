package org.solyton.solawi.bid.application.ui.page.user.data

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite

@Lensify
data class UiStateOfOrganizationPage(
    @ReadWrite val isMemberListOpened: Boolean = false,
    @ReadWrite val isApplicationListOpened: Boolean = false
)
