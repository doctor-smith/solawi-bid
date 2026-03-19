package org.solyton.solawi.bid.module.i18n.data

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.language.Lang
import org.evoleq.language.LangComponent

@Lensify data class I18N(
    @ReadWrite val locale: String = "de",
    @ReadWrite val locales: List<String> = listOf(),
    @ReadWrite val language: Lang = Lang.Block("", listOf()),
    @ReadWrite val loadedComponents: Set<LangComponent> = emptySet(),
    @ReadWrite val loadingComponents: Set<LangComponent> = emptySet(),
)
