package org.solyton.solawi.bid.module.user.action.permission

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.permission.data.api.ApiContexts
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUser
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.service.getSubjectFromJwt

fun readUserPermissionsAction(nameSuffix: String = ""): Action<Application, ReadRightRoleContextsOfUser, ApiContexts> = Action(
    name = "ReadUserPermissions$nameSuffix",
    reader = Reader{app: Application -> ReadRightRoleContextsOfUser(getSubjectFromJwt( app.user.accessToken ) !!) },
    endPoint = ReadRightRoleContextsOfUser::class,
    writer = { contexts: ApiContexts -> {app: Application -> app.copy(
        user = app.user.copy(
            permissions = Permissions(
                contexts = contexts.list.map {
                    it.toDomainType()
                })
        )
    ) }}
)
