package org.solyton.solawi.bid.module.user.action.permission

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUsers
import org.solyton.solawi.bid.module.permission.data.api.UserToContextsMap
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.Application


fun readPermissionsOfUsersAction(nameSuffix: String = ""): Action<Application, ReadRightRoleContextsOfUsers, UserToContextsMap> = Action(
    name = "ReadPermissionsOfUsers$nameSuffix",
    reader = Reader{app: Application -> ReadRightRoleContextsOfUsers(app.managedUsers.map {
        user -> user.id
    }) },
    endPoint = ReadRightRoleContextsOfUsers::class,
    writer = { contexts: UserToContextsMap -> {app: Application -> app.copy(
        managedUsers = app.managedUsers.map { user -> user.copy(
            permissions = Permissions(
                contexts = contexts.map[user.id]!!.map {
                    it.toDomainType()
                })
        )}
    ) }}
)
