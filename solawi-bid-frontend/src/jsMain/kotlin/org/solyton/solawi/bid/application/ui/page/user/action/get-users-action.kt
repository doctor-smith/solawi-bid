package org.solyton.solawi.bid.application.ui.page.user.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.api.GetUsers
import org.solyton.solawi.bid.module.user.data.api.Users
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser

@Markup
fun getUsers() = Action<Application, GetUsers, Users>(
    name = "GetUsers",
    reader = Reader { _: Application -> GetUsers },
    endPoint = GetUsers::class,
    writer = managedUsers.set contraMap{
        users: Users -> users.all.map{ ManagedUser(it.id, it.username, "", Permissions())
    } }
)