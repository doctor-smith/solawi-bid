package org.solyton.solawi.bid.module.process.data.history

import org.evoleq.axioms.definition.Lensify

// todo:dev implement process history

@Lensify
data class ProcessHistory(
    val map: Map<String, List<String>> = mapOf()
)
