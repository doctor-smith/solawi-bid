package org.solyton.solawi.bid.module.user.action.organization


import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.RemoveMember
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

fun removeMember(
    // uuid string
    memberId: String,
    organization: Lens<Application, Organization>
) : Action<Application, RemoveMember, ApiOrganization> = Action(
    name = "RemoveMember",
    endPoint = RemoveMember::class,
    reader = organization * Reader {
        o:Organization -> RemoveMember(o.organizationId,memberId)
    },
    writer = organization.set contraMap { apiOrganization: ApiOrganization -> apiOrganization.toDomainType() }
)
