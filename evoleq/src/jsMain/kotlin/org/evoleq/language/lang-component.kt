package org.evoleq.language

import org.evoleq.math.Reader

interface BasePath {
    val value: String
}

interface LangComponent : BasePath{
    val path: String
}


@I18N
val component: (LangComponent)->Reader<Lang, Lang.Block> = { c -> Reader { lang -> (lang as Lang.Block).component(c.path) } }

@I18N
val subComp: (String)->Reader<Lang, Lang.Block> = {c -> Reader { lang -> (lang as Lang.Block).component(c) }}
