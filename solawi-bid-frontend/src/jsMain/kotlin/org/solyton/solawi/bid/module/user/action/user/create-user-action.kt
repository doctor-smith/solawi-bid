package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.transform.toNewManagedType

const val CREATE_USER = "CreateUser"

@Markup
fun createUser(user: CreateUser) =
    Action<Application, CreateUser, User>(
        name = CREATE_USER,
        reader = Reader { _: Application -> user },
        endPoint = CreateUser::class,
        // todo:dev improve
        writer = managedUsers.add() contraMap {user -> user.toNewManagedType()}
    )
