package org.solyton.solawi.bid.module.application.i18n

import org.evoleq.language.I18N
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times

typealias ApplicationComponent = Component

val name: Reader<Lang.Block, Lang.Block> = subComp("name")
val inputs: Reader<Lang.Block, Lang.Block> = subComp("inputs")

@I18N
object Component {
    val application: Reader<Lang.Block, Lang.Block> = subComp("application")
    val applications: Reader<Lang.Block, Lang.Block> = subComp("applications")
    val base: Reader<Lang, Lang.Block> = subComp(BASE_PATH)
    val modules: Reader<Lang.Block, Lang.Block> = subComp("modules")
    val module: Reader<Lang.Block, Lang.Block> = subComp("module")
    val actions: Reader<Lang.Block, Lang.Block> = subComp("actions")
    val action: Reader<Lang.Block, Lang.Block> = subComp("action")
    val details: Reader<Lang.Block, Lang.Block> = subComp("details")
    val linkedOrganizations: Reader<Lang.Block, Lang.Block> = subComp("linkedOrganizations")
    val role: Reader<Lang.Block, Lang.Block> = subComp("role")
    val roles: Reader<Lang.Block, Lang.Block> = subComp("roles")
    val rights: Reader<Lang.Block, Lang.Block> = subComp("rights")
    val right: Reader<Lang.Block, Lang.Block> = subComp("right")
    val organization: Reader<Lang.Block, Lang.Block> = subComp("organization")
    val headers: Reader<Lang.Block, Lang.Block> = subComp("headers")
    val navToAppManPage: Reader<Lang.Block, Lang.Block> = subComp("navToAppManPage")
    val navToParentApplication: Reader<Lang.Block, Lang.Block> = subComp("navToParentApplication")
    val listOfModules: Reader<Lang.Block, Lang.Block> = subComp("listOfModules")

    val listOfApplications: Reader<Lang.Block, Lang.Block> = subComp("listOfApplications")
    val defaultContext: Reader<Lang.Block, Lang.Block> = subComp("defaultContext")
    val showDetails: Reader<Lang.Block, Lang.Block> = subComp("showDetails")
    val edit: Reader<Lang.Block, Lang.Block> = subComp("edit")
    val editRole: Reader<Lang.Block, Lang.Block> = subComp("editRole")
    val editRight: Reader<Lang.Block, Lang.Block> = subComp("editRight")
    val editContext: Reader<Lang.Block, Lang.Block> = subComp("editContext")

    fun applicationName(texts: Source<Lang.Block>): Reader<String, Lang.Block> =
        Reader{ key: String -> application(key)(texts.emit()) }
}

@I18N
fun application(key: String): Reader<Lang.Block, Lang.Block> = subComp(key.camelCase())

//@I18N

// fun <T> Reader<T, String>.times(reader: Reader<String, Lang.Block>: Reader<Lang.Block, Lang.Block> = this * Component.application
@I18N
fun module(appKey: String, moduleKey: String): Reader<Lang.Block, Lang.Block> =
    subComp(appKey.camelCase()) *
    Component.modules *
    subComp(moduleKey.camelCase())

