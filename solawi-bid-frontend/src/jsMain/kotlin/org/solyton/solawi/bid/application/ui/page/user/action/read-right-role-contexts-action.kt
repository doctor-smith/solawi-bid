package org.solyton.solawi.bid.application.ui.page.user.action

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.availablePermissions
import org.solyton.solawi.bid.module.permission.data.api.ApiContexts
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContexts
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.permissions.data.transform.structureBy
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType

fun readRightRoleContextsAction(nameSuffix: String, parentChildRelations: ParentChildRelationsOfContexts): Action<Application, ReadRightRoleContexts, ApiContexts > = Action(
    name = "ReadRightRoleContexts$nameSuffix",
    reader = Reader {app: Application -> ReadRightRoleContexts(parentChildRelations.list.map { it.contextId })},
    endPoint = ReadRightRoleContexts::class,
    writer = availablePermissions.set contraMap {contexts: ApiContexts ->
        Permissions(
            userId = "",
            contexts = contexts.list.map { it.toDomainType() }.structureBy(parentChildRelations)
        )

    }
)
