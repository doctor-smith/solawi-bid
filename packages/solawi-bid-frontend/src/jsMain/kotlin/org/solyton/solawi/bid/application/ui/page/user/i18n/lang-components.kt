package org.solyton.solawi.bid.application.ui.page.user.i18n

import org.evoleq.language.LangComponent

private const val basePath = "solyton.user"

sealed class UserLangComponent(override val path: String, override val value: String = basePath) : LangComponent {
    data object UserManagementPage : UserLangComponent("$basePath.managementPage")
    data object UserPrivatePage : UserLangComponent("$basePath.privatePage")
}