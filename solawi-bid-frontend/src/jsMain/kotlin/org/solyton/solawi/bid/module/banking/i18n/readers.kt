package org.solyton.solawi.bid.module.banking.i18n

import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.math.Reader

typealias Component = Reader<Lang.Block, Lang.Block>
typealias Value = Reader<Lang.Block, String>

// Components
val debtorName: Component = { block -> block.component("debtorName") }
val mandateReference: Component = { block -> block.component("mandateReference") }
val dateSigned: Component = { block -> block.component("dateSigned") }
val executionDate: Component = { block -> block.component("executionDate") }
val failureReason: Component = { block -> block.component("failureReason") }
val label: Component = { block -> block.component("label") }
val sequenceType: Component = { block -> block.component("sequenceType") }
val seqType: Component = { block -> block.component("seqType") }
val status: Component = { block -> block.component("status") }
val totalAmount: Component = {block -> block.component("totalAmount")}
val updateSepaPaymentForm: Component = { block -> block.component("updateSepaPaymentForm") }


// Values
val title: Value = {block -> block["title"]}

