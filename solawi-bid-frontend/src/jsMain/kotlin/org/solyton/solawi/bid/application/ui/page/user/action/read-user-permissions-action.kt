package org.solyton.solawi.bid.application.ui.page.user.action

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.permission.data.api.Contexts as ApiContexts
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUser
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.permissions.data.Right
import org.solyton.solawi.bid.module.permissions.data.Role
import org.solyton.solawi.bid.module.user.service.getSubjectFromJwt

fun readUserPermissionsAction(): Action<Application, ReadRightRoleContextsOfUser, ApiContexts > = Action(
    name = "ReadUserPermissions",
    reader = Reader{app: Application -> ReadRightRoleContextsOfUser(getSubjectFromJwt( app.userData.accessToken ) !!) },
    endPoint = ReadRightRoleContextsOfUser::class,
    writer = { contexts: ApiContexts -> {app: Application -> app.copy(
        userData = app.userData.copy(
            permissions = Permissions(
                contexts = contexts.list.map {
                    Context(
                        it.id,
                        it.name,
                        it.roles.map{role ->
                            Role(
                                role.id,
                                role.name,
                                role.description,
                                role.rights.map { right ->
                                    Right(
                                        right.id,
                                        right.name,
                                        right.description
                                    )
                                }
                            )
                        }
                    )
                })
        )
    ) }}
)
