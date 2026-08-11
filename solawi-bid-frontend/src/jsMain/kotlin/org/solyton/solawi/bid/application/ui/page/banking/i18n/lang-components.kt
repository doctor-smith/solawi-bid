package org.solyton.solawi.bid.application.ui.page.banking.i18n

import org.evoleq.language.LangComponent

private const val BASE_PATH = "solyton.banking"

sealed class BankingLangComponent(
    override val path: String,
    override val value: String = BASE_PATH
) : LangComponent {
    data object BankingForOrganizationsPage : BankingLangComponent(
        "$BASE_PATH.bankingForOrganizationsPage",
        "$BASE_PATH.bankingForOrganizationsPage"
    )
}
