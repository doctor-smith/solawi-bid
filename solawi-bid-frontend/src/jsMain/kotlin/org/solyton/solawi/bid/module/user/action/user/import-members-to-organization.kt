package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations

@Markup
fun importMembersToOrganization(importMembers: ImportMembers, nameSuffix: String = ""): Action<Application, ImportMembers, ApiOrganization> = Action(
    // todo:test write api test
    name = "ImportMembers$nameSuffix",
    reader = Reader { _: Application -> importMembers },
    endPoint = ImportMembers::class,
    writer = (user * organizations * DeepSearch {
        it.organizationId == importMembers.organizationId
    }).set contraMap {
        organization -> organization.toDomainType()
    }
)
