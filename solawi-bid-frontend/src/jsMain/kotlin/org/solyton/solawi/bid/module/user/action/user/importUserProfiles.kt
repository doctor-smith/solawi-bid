package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles

@Markup
fun importUserProfiles(userProfiles: ImportUserProfiles, nameSuffix: String = ""): Action<Application, ImportUserProfiles, Unit> = Action(
    // todo:test write api test
    name = "ImportUserProfiles$nameSuffix",
    reader = Reader { _: Application -> userProfiles },
    endPoint = ImportUserProfiles::class,
    writer = {_: Unit -> {app: Application -> app}}
)
