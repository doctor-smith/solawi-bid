package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.math.Append
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.organization.subOrganizations
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

fun createChildOrganization(name: String, parent: Lens<Application, Organization>): Action<Application, CreateChildOrganization, ApiOrganization> = Action(
    name = "CreateChildOrganization",
    reader = parent * Reader {
        organization: Organization ->  CreateChildOrganization(
            organizationId = organization.organizationId,
            name = name
        )
    },
    endPoint = CreateChildOrganization::class,
    writer = (parent * subOrganizations * Append<Organization>()) contraMap {
        organization: ApiOrganization -> organization.toDomainType()
    }
)
