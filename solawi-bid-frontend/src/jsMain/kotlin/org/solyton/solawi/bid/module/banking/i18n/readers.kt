package org.solyton.solawi.bid.module.banking.i18n

import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.math.Reader

typealias Component = Reader<Lang.Block, Lang.Block>
typealias Value = Reader<Lang.Block, String>

val debtorName: Component = { block -> block.component("debtorName") }
val mandateReference: Component = { block -> block.component("mandateReference") }
val dateSigned: Component = { block -> block.component("dateSigned") }
val status: Component = { block -> block.component("status") }
val label: Component = { block -> block.component("label") }
val title: Value = {block -> block["title"]}