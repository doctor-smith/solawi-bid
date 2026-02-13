package org.solyton.solawi.bid.module.process.data.process

import kotlinx.datetime.Clock
import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite

@Lensify data class Process(
    @ReadOnly val id: String,
    @ReadOnly val parentId: String? = null,
    @ReadOnly val name: String? = null,
    @ReadWrite val state: ProcessState = ProcessState.Registered,
    @ReadOnly val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    @ReadWrite val updatedAt: Long? = null
)
