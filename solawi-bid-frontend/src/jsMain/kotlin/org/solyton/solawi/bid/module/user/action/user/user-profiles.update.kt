package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.math.Writer
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.liftBy
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.UserApplication
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfile
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.transform.toDomainType


const val UPDATE_USER_PROFILE = "CreateUserProfile"

fun updateUserProfile(
    data: UpdateUserProfile,
    nameSuffix: String? = null
): Action<UserApplication, UpdateUserProfile, ApiUserProfile> = Action(
    name = UPDATE_USER_PROFILE.suffixed(nameSuffix),
    reader = { _: UserApplication -> data },
    endPoint = UpdateUserProfile::class,
    writer = managedUsers * (Writer {
        // write userprofile to user:
        // As there might be users without profiles,
        // we need to use an appropriate writer,
        // which allows us to ignore these cases
            uP: UserProfile? -> { m: ManagedUser -> when{
        uP == null -> m
        else -> m.copy(profile = uP.toDomainType())
    }  }
    } liftBy {
        // select user profile.
        user, profiles -> profiles.firstOrNull{ it?.userId == user.id }
    }) contraMap { listOf(it) }
)
