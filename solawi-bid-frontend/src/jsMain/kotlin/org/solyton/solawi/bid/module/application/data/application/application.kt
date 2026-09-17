package org.solyton.solawi.bid.module.application.data.application

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.module.Module

@Lensify data class Application(
    @ReadWrite val id: String,
    @ReadWrite val name: String,
    @ReadWrite val state: LifecycleStage,
    @ReadWrite val modules: List<Module> = listOf()
)
