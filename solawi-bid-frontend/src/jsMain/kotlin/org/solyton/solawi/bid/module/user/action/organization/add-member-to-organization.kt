package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.math.Reader
import org.evoleq.math.branch
import org.evoleq.math.contraMap
import org.evoleq.math.times
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.AddMember
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

const val ADD_MEMBER_TO_ORGANIZATION = "AddMemberToOrganization"

fun addMember(
    // uuid string
    memberId: String,
    // list of uuid strings
    roles: List<String>,
    organization: Lens<Application, Organization>
) : Action<Application, AddMember, ApiOrganization> = Action(
    name = ADD_MEMBER_TO_ORGANIZATION,
    endPoint = AddMember::class,
    reader = organization * Reader {
        o:Organization -> AddMember(o.organizationId,memberId, roles)
    },
    writer = organization.set contraMap { apiOrganization: ApiOrganization -> apiOrganization.toDomainType() }
)


fun addMember(
    memberId: Reader<Application, String>,
    // list of uuid strings
    roles: List<String>,
    organization: Lens<Application, Organization>
) : Action<Application, AddMember, ApiOrganization> = Action(
    name = ADD_MEMBER_TO_ORGANIZATION,
    endPoint = AddMember::class,
    reader = (organization.get branch memberId) * Reader {
            pair -> AddMember(pair.first.organizationId,pair.second, roles)
    },
    writer = organization.set contraMap { apiOrganization: ApiOrganization -> apiOrganization.toDomainType() }
)
