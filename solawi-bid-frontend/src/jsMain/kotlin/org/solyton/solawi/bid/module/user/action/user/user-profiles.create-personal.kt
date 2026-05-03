package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.UserApplication
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateUserProfile
import org.solyton.solawi.bid.module.user.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.profile

const val CREATE_PERSONAL_USER_PROFILE = "CREATE_PERSONAL_USER_PROFILE"

fun createPersonalUserProfile(
    data: CreateUserProfile,
    nameSuffix: String? = null
): Action<UserApplication, CreateUserProfile, ApiUserProfile> = Action(
    name = CREATE_PERSONAL_USER_PROFILE.suffixed(nameSuffix),
    reader = { _: UserApplication -> data },
    endPoint = CreateUserProfile::class,
    writer = user * profile.set contraMap { it.toDomainType() }
)
