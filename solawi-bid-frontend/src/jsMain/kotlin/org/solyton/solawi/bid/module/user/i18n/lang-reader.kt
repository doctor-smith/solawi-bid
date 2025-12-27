package org.solyton.solawi.bid.module.user.i18n

import org.evoleq.language.I18N
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.math.Reader

@I18N
object Component {
    val actions: Reader<Lang.Block, Lang.Block> = subComp("actions")
    val address : Reader<Lang.Block, Lang.Block> = subComp("address")
    val edit: Reader<Lang.Block, Lang.Block> = subComp("edit")

    val headers : Reader<Lang.Block, Lang.Block> = subComp("headers")
    val listOfMembers: Reader<Lang.Block, Lang.Block> = subComp("listOfMembers")

    val name: Reader<Lang.Block, Lang.Block> = subComp("name")
    val navToManagementPage: Reader<Lang.Block, Lang.Block> = subComp("navToManagementPage")

    val organization: Reader<Lang.Block, Lang.Block> = subComp("organization")
    val organizations: Reader<Lang.Block, Lang.Block> = subComp("organizations")
    val roles: Reader<Lang.Block, Lang.Block> = subComp("roles")
    val standard: Reader<Lang.Block, Lang.Block> = subComp("standard")
    val username : Reader<Lang.Block, Lang.Block> = subComp("username")
    val userProfile: Reader<Lang.Block, Lang.Block> = subComp("userProfile")
}
