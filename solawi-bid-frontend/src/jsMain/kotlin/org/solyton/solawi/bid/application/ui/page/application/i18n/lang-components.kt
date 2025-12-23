package org.solyton.solawi.bid.application.ui.page.application.i18n

import org.evoleq.language.LangComponent

const val BASE_PATH = "solyton.application"

sealed class ApplicationLangComponent(override val path: String, override val value: String = BASE_PATH) : LangComponent {
    data object PrivateApplicationManagementPage : ApplicationLangComponent(
        "$BASE_PATH.private.managementPage"
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

fun String.camelCase() = split("_").mapIndexed { index, s ->
    if(index==0) s.lowercase() else s.first().uppercase() + s.drop(1).lowercase()
}.joinToString("")
