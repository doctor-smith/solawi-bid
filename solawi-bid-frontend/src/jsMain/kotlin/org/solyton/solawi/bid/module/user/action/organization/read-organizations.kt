package org.solyton.solawi.bid.module.user.action.organization

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganizations
import org.solyton.solawi.bid.module.user.data.api.organization.ReadOrganizations
import org.solyton.solawi.bid.module.user.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations


fun readOrganizations(): Action<Application, ReadOrganizations, ApiOrganizations> = Action(
    name = "ReadOrganizations",
    reader = { ReadOrganizations },
    endPoint = ReadOrganizations::class,
    writer = (user * organizations.set) contraMap {
        organizations: ApiOrganizations -> organizations.toDomainType()
    }
)
