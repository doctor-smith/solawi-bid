package org.solyton.solawi.bid.module.application.data.organizationapplication

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.application.data.application.Application

@Lensify data class OrganizationApplications(
    @ReadWrite val organizationId: String,
    @ReadWrite val applications: List<Application>
)
