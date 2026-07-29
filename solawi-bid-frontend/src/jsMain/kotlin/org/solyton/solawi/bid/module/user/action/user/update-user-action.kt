package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.seq
import org.evoleq.optics.transform.times
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.UpdateUser
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.transform.toNewManagedType
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.username

const val UPDATE_USER = "UPDATE_USER"

fun updateUser(data: UpdateUser, nameSuffix: String = ""): Action<Application, UpdateUser, User> = Action(
    name = UPDATE_USER.suffixed(nameSuffix),
    reader = Reader { _: Application -> data },
    endPoint = UpdateUser::class,
    writer = seq(
        managedUsers.update{
            // the second arg is the one to update !!
            _, toUpdate -> toUpdate.username == data.oldUsername.value
        } contraMap {user -> user.toNewManagedType()},
        user * username.set contraMap {user -> user.username}
    )
)
