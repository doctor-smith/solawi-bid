package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.AddMember
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

fun addMember(
    // uuid string
    memberId: String,
    // list of uuid strings
    roles: List<String>,
    organization: Lens<Application, Organization>
) : Action<Application, AddMember, ApiOrganization> = Action(
    name = "AddMember",
    endPoint = AddMember::class,
    reader = organization * Reader {
        o:Organization -> AddMember(o.organizationId,memberId, roles)
    },
    writer = organization.set contraMap { apiOrganization: ApiOrganization -> apiOrganization.toDomainType() }
)
