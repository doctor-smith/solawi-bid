package org.solyton.solawi.bid.module.shares.i18n

import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.math.Reader

typealias Component = Reader<Lang.Block, Lang.Block>
typealias Value = Reader<Lang.Block, String>

object LangForm {
    val inputs: Component = { block -> block.component("inputs") }
}

object LangField{
    val actions: Component = { block -> block.component("actions") }
    val ahcAuthorized: Component = { block -> block.component("ahcAuthorized") }
    val coSubscribers: Component = { block -> block.component("coSubscribers") }
    val distributionPoint: Component = { block -> block.component("distributionPoint") }
    val fiscalYear: Component = { block -> block.component("fiscalYear") }
    val fullName: Component = { block -> block.component("fullName") }
    val header: Component = { block -> block.component("header") }
    val listOfUsers: Component = { block -> block.component("listOfUsers") }
    val messages: Component = { block -> block.component("messages") }
    val numberOfShares: Component = { block -> block.component("numberOfShares") }

    val pricePerShare: Component = { block -> block.component("pricePerShare") }
    val searchBox: Component = { block -> block.component("searchBox") }
    val selectUser: Component = { block -> block.component("selectUser") }
    val shareOffer: Component = { block -> block.component("shareOffer") }
    val shareStatus: Component = { block -> block.component("shareStatus") }
    val shareSubscription: Component = { block -> block.component("shareSubscription") }
    val tooltip: Component = { block -> block.component("tooltip") }
    val user: Component = { block -> block.component("user") }
    val username: Component = { block -> block.component("username") }

    // Values
    val forbiddenUsers: Value = { lang -> lang["forbiddenUsers"] }
    val placeholder: Value = { lang -> lang["placeholder"] }

}
