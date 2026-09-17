package org.solyton.solawi.bid.module.application.data.module

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.application.data.ApiLifecycleStage

@Lensify data class Module(
    @ReadOnly val id: String,
    @ReadWrite val name: String,
    @ReadWrite val state: ApiLifecycleStage
)
