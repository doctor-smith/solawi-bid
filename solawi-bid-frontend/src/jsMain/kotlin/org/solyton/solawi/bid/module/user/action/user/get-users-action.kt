package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.GetUsers
import org.solyton.solawi.bid.module.user.data.api.Users
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser

@Markup
fun getUsers() = Action<Application, GetUsers, Users>(
    name = "GetUsers",
    reader = Reader { _: Application -> GetUsers },
    endPoint = GetUsers::class,
    writer = {users: Users ->
        {app: Application ->
            app.copy(managedUsers = users.all.map{ managedUser ->
                val user = app.managedUsers.firstOrNull{it.id == managedUser.id}
                user ?: ManagedUser(managedUser.id, managedUser.username, "", Permissions())
            })}
    }
)
