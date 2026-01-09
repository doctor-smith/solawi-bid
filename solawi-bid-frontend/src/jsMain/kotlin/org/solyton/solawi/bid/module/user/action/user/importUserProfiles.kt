package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.liftBy
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.userprofile.ApiUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfile
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.managed.profile
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.transform.toDomainType

@Markup
fun importUserProfiles(userProfiles: ImportUserProfiles, nameSuffix: String = ""): Action<Application, ImportUserProfiles, ApiUserProfiles> = Action(
    // todo:test write api test
    name = "ImportUserProfiles$nameSuffix",
    reader = Reader { _: Application -> userProfiles },
    endPoint = ImportUserProfiles::class,
    writer = managedUsers * (Writer {
        // write userprofile to user
        uP: UserProfile -> { m: ManagedUser -> m.profile { uP.toDomainType() } }
    } liftBy {
        // select user profile
        user, profiles -> profiles.first{it.userId == user.id}
    }) contraMap {it.all}
)
