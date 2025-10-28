package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.math.Push
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations

fun createOrganization(name: String): Action<Application, CreateOrganization, ApiOrganization> = Action(
    name = "CreateOrganization",
    reader = { CreateOrganization(name) },
    endPoint = CreateOrganization::class,
    writer = (user * organizations * Push<Organization>()) contraMap {
        organization: ApiOrganization -> organization.toDomainType()
    }
)
