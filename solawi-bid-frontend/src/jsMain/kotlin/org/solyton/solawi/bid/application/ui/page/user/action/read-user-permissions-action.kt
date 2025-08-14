package org.solyton.solawi.bid.application.ui.page.user.action

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUser
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.service.getSubjectFromJwt
import org.solyton.solawi.bid.module.permission.data.api.Contexts as ApiContexts

fun readUserPermissionsAction(nameSuffix: String = ""): Action<Application, ReadRightRoleContextsOfUser, ApiContexts > = Action(
    name = "ReadUserPermissions$nameSuffix",
    reader = Reader{app: Application -> ReadRightRoleContextsOfUser(getSubjectFromJwt( app.userData.accessToken ) !!) },
    endPoint = ReadRightRoleContextsOfUser::class,
    writer = { contexts: ApiContexts -> {app: Application -> app.copy(
        userData = app.userData.copy(
            permissions = Permissions(
                contexts = contexts.list.map {
                    it.toDomainType()
                })
        )
    ) }}
)
