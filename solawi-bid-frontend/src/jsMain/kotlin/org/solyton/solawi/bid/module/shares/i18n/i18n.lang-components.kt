package org.solyton.solawi.bid.module.shares.i18n

import org.evoleq.language.LangComponent

const val SHARE_APPLICATION_BASE_PATH = "solyton.application.shareManagement"

sealed class ShareManagementLangComponent(
    override val path: String,
    override val value: String = SHARE_APPLICATION_BASE_PATH
) : LangComponent {
    data object Base : ShareManagementLangComponent(
        SHARE_APPLICATION_BASE_PATH
    )
}
