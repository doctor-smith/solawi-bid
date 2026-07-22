package org.solyton.solawi.bid.module.navbar.data.environment

import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.optics.Lensify

@Lensify
data class Environment(
    @ReadOnly val type: String
)
