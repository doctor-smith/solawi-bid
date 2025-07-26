package org.solyton.solawi.bid.application.ui.page.user.action


import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.api.ChangePassword
import org.solyton.solawi.bid.module.user.data.api.User

@Markup
fun changePassword(user: ChangePassword) =
    Action<Application, ChangePassword, User>(
        name = "ChangePassword",
        reader = Reader { _: Application -> user },
        endPoint = ChangePassword::class,
        // todo:dev improve
        writer = {_: User -> {app: Application -> app}}
    )