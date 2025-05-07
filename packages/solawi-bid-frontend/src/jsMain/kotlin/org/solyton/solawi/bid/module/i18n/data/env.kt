package org.solyton.solawi.bid.module.i18n.data

import org.evoleq.language.Lang

data class I18nResources(
    val url: String,
    val port: Int
)

data class Environment(
    val i18nResources: I18nResources
)

data class ComponentLookup(
    val component: Lang.Block,
    val language: Lang,
    val mergeNeeded: Boolean = false
)