package org.solyton.solawi.bid.module.process.data.process

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite

@Lensify data class Process(
    @ReadOnly val id: String,
    @ReadOnly val name: String? = null,
    @ReadWrite val state: ProcessState = ProcessState.Active,
)
