package org.solyton.solawi.bid.module.modal.i18n

import org.evoleq.language.LangComponent

private const val BASE_PATH = "solyton.modal"

sealed class ModalLangComponent(
    override val path: String,
    override val value: String = BASE_PATH
): LangComponent {
    object Default : ModalLangComponent(
        "$BASE_PATH.default",
        "$BASE_PATH.default",
    )
}
