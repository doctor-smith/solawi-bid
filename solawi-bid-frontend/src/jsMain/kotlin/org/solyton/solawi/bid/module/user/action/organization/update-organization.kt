package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateOrganization
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

fun updateOrganization(name: String, organization: Lens<Application, Organization>): Action<Application, UpdateOrganization, ApiOrganization> = Action(
    name = "UpdateOrganization",
    reader = organization * Reader { organization: Organization ->
        UpdateOrganization(
            organization.organizationId,
            name
        )
    },
    endPoint = UpdateOrganization::class,
    writer = organization.set contraMap {
        organization: ApiOrganization -> organization.toDomainType()
    }
)
