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
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.profile


const val UPDATE_PERSONAL_USER_PROFILE = "UPDATE_PERSONAL_USER_PROFILE"

fun updatePersonalUserProfile(
    data: UpdateUserProfile,
    nameSuffix: String? = null
): Action<UserApplication, UpdateUserProfile, ApiUserProfile> = Action(
    name = UPDATE_USER_PROFILE.suffixed(nameSuffix),
    reader = { _: UserApplication -> data },
    endPoint = UpdateUserProfile::class,
    writer = user * profile.set contraMap { it.toDomainType() }
)
