package org.solyton.solawi.bid.module.i18n.data

import org.evoleq.language.Lang
import org.evoleq.language.LangComponent
import org.evoleq.language.subComp
import org.evoleq.math.Reader

val componentLoaded: (component: LangComponent) ->  Reader<I18N, Boolean> = {component -> Reader{
    i18n: I18N -> i18n.loadedComponents.contains(component)
}}

@org.evoleq.language.I18N
val buttons: Reader<Lang.Block, Lang.Block> = subComp("buttons")
