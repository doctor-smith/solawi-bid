package org.solyton.solawi.bid.module.application.i18n

import org.evoleq.language.I18N
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.math.Reader
import org.evoleq.math.times
import org.solyton.solawi.bid.application.ui.page.application.i18n.camelCase

val name: Reader<Lang.Block, Lang.Block> = subComp("name")
val inputs: Reader<Lang.Block, Lang.Block> = subComp("inputs")
object Component {
    val application: Reader<Lang.Block, Lang.Block> = subComp("application")
    val applications: Reader<Lang.Block, Lang.Block> = subComp("applications")
    val modules: Reader<Lang.Block, Lang.Block> = subComp("modules")
    val module: Reader<Lang.Block, Lang.Block> = subComp("module")
    val actions: Reader<Lang.Block, Lang.Block> = subComp("actions")
    val action: Reader<Lang.Block, Lang.Block> = subComp("action")
    val details: Reader<Lang.Block, Lang.Block> = subComp("details")
    val role: Reader<Lang.Block, Lang.Block> = subComp("role")
    val roles: Reader<Lang.Block, Lang.Block> = subComp("roles")
    val rights: Reader<Lang.Block, Lang.Block> = subComp("rights")
    val right: Reader<Lang.Block, Lang.Block> = subComp("right")
    val organization: Reader<Lang.Block, Lang.Block> = subComp("organization")
    val headers: Reader<Lang.Block, Lang.Block> = subComp("headers")
    val navToAppManPage: Reader<Lang.Block, Lang.Block> = subComp("navToAppManPage")
    val navToParentApplication: Reader<Lang.Block, Lang.Block> = subComp("navToParentApplication")
    val listOfModules: Reader<Lang.Block, Lang.Block> = subComp("listOfModules")
    val defaultContext: Reader<Lang.Block, Lang.Block> = subComp("defaultContext")
    val showDetails: Reader<Lang.Block, Lang.Block> = subComp("showDetails")
    val edit: Reader<Lang.Block, Lang.Block> = subComp("edit")
    val editRole: Reader<Lang.Block, Lang.Block> = subComp("editRole")
    val editRight: Reader<Lang.Block, Lang.Block> = subComp("editRight")
    val editContext: Reader<Lang.Block, Lang.Block> = subComp("editContext")

}

@I18N
fun application(key: String): Reader<Lang.Block, Lang.Block> = subComp(key.camelCase())
@I18N
fun module(appKey: String, moduleKey: String): Reader<Lang.Block, Lang.Block> =
    subComp(appKey.camelCase()) *
    Component.modules *
    subComp(moduleKey.camelCase())

