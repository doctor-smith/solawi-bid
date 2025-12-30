package org.solyton.solawi.bid.application.ui.page.application.i18n

import org.evoleq.language.LangComponent
import org.solyton.solawi.bid.module.application.i18n.BASE_PATH
import org.solyton.solawi.bid.module.application.i18n.camelCase


sealed class ApplicationLangComponent(override val path: String, override val value: String = BASE_PATH) : LangComponent {
    data object PrivateApplicationManagementPage : ApplicationLangComponent(
        "$BASE_PATH.private.managementPage"
    )
    data object PrivateApplicationOrganizationManagementPage : ApplicationLangComponent(
        "$BASE_PATH.private.applicationOrganizationManagementPage"
    )

    data object ApplicationManagementPage : ApplicationLangComponent(
        "$BASE_PATH.management.applicationManagementPage"
    )

    data object ApplicationPage : ApplicationLangComponent(
        "$BASE_PATH.management.applicationPage"
    )

    data object ModulePage : ApplicationLangComponent(
        "$BASE_PATH.management.modulePage"
    )

    data class ApplicationDetails(val key: String) : ApplicationLangComponent(
        "$BASE_PATH.${key.lowercase().camelCase()}",
        "$BASE_PATH.${key.lowercase().camelCase()}"
    )

}
