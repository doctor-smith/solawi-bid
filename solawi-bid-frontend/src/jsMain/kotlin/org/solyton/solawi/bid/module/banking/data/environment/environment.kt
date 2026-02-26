package org.solyton.solawi.bid.module.banking.data.environment

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly


@Lensify
data class Environment(
    @ReadOnly val type: String = "DEV",
    @ReadOnly val frontendUrl: String = "http://localhost",
    @ReadOnly val frontendPort: Int = 8080,
    @ReadOnly val backendUrl: String = "http://localhost",
    @ReadOnly val backendPort: Int = 8081,
)
