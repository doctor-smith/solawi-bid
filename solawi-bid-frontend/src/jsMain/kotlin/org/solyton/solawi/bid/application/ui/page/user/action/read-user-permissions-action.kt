package org.solyton.solawi.bid.application.ui.page.user.action

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.permission.data.api.Context as ApiContext
import org.solyton.solawi.bid.module.permission.data.api.ReadRightRoleContextsOfUser
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.permissions.data.Right
import org.solyton.solawi.bid.module.permissions.data.Role
import org.solyton.solawi.bid.module.user.service.getSubjectFromJwt

fun readUserPermissionsAction(): Action<Application, ReadRightRoleContextsOfUser, List<ApiContext> > = Action(
    name = "ReadUserPermissions",
    reader = Reader{app: Application -> ReadRightRoleContextsOfUser(getSubjectFromJwt( app.userData.accessToken ) !!) },
    endPoint = ReadRightRoleContextsOfUser::class,
    writer = { contexts: List<ApiContext> -> {app: Application -> app.copy(
        userData = app.userData.copy(
            permissions = Permissions(
                contexts = contexts.map {
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
