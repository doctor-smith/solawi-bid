package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.compose.Markup
import org.evoleq.math.Writer
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.liftBy
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.ReadUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfile
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

const val READ_USER_PROFILES = "ReadUserProfiles"

/**
 * Creates an action to fetch user profile data.
 *
 * @param nameSuffix An optional suffix to append to the generated action name. Defaults to an empty string.
 * @param userIds A list of user IDs for which profiles should be fetched.
 * @return An `Action` that fetches and writes user profile data for the given user IDs.
 */
@Markup
fun readUserProfiles(userIds: List<String>, nameSuffix: String = "", ): Action<Application, ReadUserProfiles, ApiUserProfiles> = Action(
    name = "$READ_USER_PROFILES$nameSuffix",
    reader = {_: Application -> ReadUserProfiles(userIds)},
    endPoint = ReadUserProfiles::class,
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
    }) contraMap {it.all}
)
