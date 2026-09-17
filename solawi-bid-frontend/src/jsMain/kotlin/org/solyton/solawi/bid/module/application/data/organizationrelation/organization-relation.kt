package org.solyton.solawi.bid.module.application.data.organizationrelation

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite

@Lensify data class ApplicationOrganizationRelation(
    @ReadWrite val applicationId: String,
    @ReadWrite val organizationId: String,
    @ReadWrite val contextId: String,
    @ReadWrite val moduleIds: List<String> = listOf()
)
