package org.solyton.solawi.bid.application.ui.page.user.i18n

import org.evoleq.language.LangComponent

private const val BASE_PATH = "solyton.user"

sealed class UserLangComponent(override val path: String, override val value: String = BASE_PATH) : LangComponent {
    data object UserManagementPage : UserLangComponent("$BASE_PATH.managementPage")
    data object UserPrivatePage : UserLangComponent("$BASE_PATH.privatePage")
}

private const val ORGANIZATION_BASE_PATH = "$BASE_PATH.organization"

sealed class OrganizationLangComponent(
    override val path: String,
    override val value: String = ORGANIZATION_BASE_PATH
) : LangComponent {
    data object OrganizationManagementPage : OrganizationLangComponent("$ORGANIZATION_BASE_PATH.managementPage")

    data object OrganizationPage : OrganizationLangComponent("$ORGANIZATION_BASE_PATH.organizationPage")
}
