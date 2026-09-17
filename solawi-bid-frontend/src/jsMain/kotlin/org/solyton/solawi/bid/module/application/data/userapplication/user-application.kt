package org.solyton.solawi.bid.module.application.data.userapplication

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.application.data.application.Application

@Lensify data class UserApplications(
    @ReadWrite val userId: String,
    @ReadWrite val applications: List<Application>
)
