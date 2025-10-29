package org.solyton.solawi.bid.module.user.action.organization


import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateMember
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

fun updateMember(
    // uuid string
    memberId: String,
    // list of uuid strings
    roles: List<String>,
    organization: Lens<Application, Organization>
) : Action<Application, UpdateMember, ApiOrganization> = Action(
    name = "UpdateMember",
    endPoint = UpdateMember::class,
    reader = organization * Reader {
            o:Organization -> UpdateMember(o.organizationId,memberId, roles)
    },
    writer = organization.set contraMap { apiOrganization: ApiOrganization -> apiOrganization.toDomainType() }
)
