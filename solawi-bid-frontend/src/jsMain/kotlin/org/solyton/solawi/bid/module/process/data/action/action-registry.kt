package org.solyton.solawi.bid.module.process.data.action

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite

@Lensify
data class ActionRegistry(
    @ReadWrite val actions: Map<String, suspend () -> Unit>
): Map<String, suspend () -> Unit> by actions
