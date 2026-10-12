package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.seq
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.UpdateUsername
import org.solyton.solawi.bid.module.user.data.api.User
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.transform.toNewManagedType


const val UPDATE_USER_NAME = "UPDATE_USER_NAME"

fun updateUsername(
    // organizationId: OrganizationId,
    data: UpdateUsername,
    nameSuffix: String = ""
): Action<Application, UpdateUsername, User> = Action(
    name = UPDATE_USER_NAME.suffixed(nameSuffix),
    reader = Reader { _: Application -> data },
    endPoint = UpdateUsername::class,
    writer = seq(
        managedUsers.update{
            // the second arg is the one to update !!
            _, toUpdate -> toUpdate.username == data.oldUsername.value
        } contraMap {user -> user.toNewManagedType()},
        /*
        user * organizations * DeepSearch { it.organizationId == organizationId.value } * members.update {
            _,  toUpdate -> toUpdate.username == data.oldUsername.value
        }
        */
    )
)
