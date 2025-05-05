package org.solyton.solawi.bid.module.i18n.service

import org.evoleq.language.*
import org.evoleq.parser.ParsingResult
import org.solyton.solawi.bid.module.i18n.api.localizeResource
import org.solyton.solawi.bid.module.i18n.data.ComponentLookup
import org.solyton.solawi.bid.module.i18n.data.Environment

suspend fun Environment.readLang(component: String): Lang {
    val resultString = localizeResource(component)
    val langResult: ParsingResult<Lang> = LanguageP().run(resultString)

    val lang: Lang? = langResult.result
    require(lang != null)

    return lang
}

suspend fun Environment.componentOnDemand(langComponent: LangComponent, lang: Lang, locale: String): ComponentLookup {
    val storedComponent: Lang.Block? = try{
        component(langComponent)(lang)
    } catch(e: Exception) {
        null
    }
    if(storedComponent != null) {
        return ComponentLookup(
            storedComponent,
            lang
        )
    }
    val componentBaseBath = langComponent.value
    val loadedComponent = readLang("$locale.$componentBaseBath") as Lang.Block

    return ComponentLookup(
        loadedComponent,
        lang.merge(loadedComponent),
        true
    )
}