package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.compose.Markup
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.ReadUserProfiles
import org.solyton.solawi.bid.module.user.data.transform.toDomainType
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.profile
import org.solyton.solawi.bid.module.values.UserId

const val READ_PERSONAL_USER_PROFILE = "READ_PERSONAL_USER_PROFILE"

@Markup
fun readPersonalUserProfile(userId: UserId, nameSuffix: String = "", ): Action<Application, ReadUserProfiles, ApiUserProfiles> = Action(
    name = READ_PERSONAL_USER_PROFILE.suffixed(nameSuffix),
    reader = {_: Application ->
        ReadUserProfiles(listOf(userId.value))
    },
    endPoint = ReadUserProfiles::class,
    writer = user * profile.set contraMap { it.all.firstOrNull()?.toDomainType() }
)
