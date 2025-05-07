package org.solyton.solawi.bid.application.ui.page.login.action

import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.user.data.accessToken
import org.solyton.solawi.bid.module.user.data.refreshToken

@Markup
val loginAction: Action<Application, Login, LoggedIn> by lazy {
    Action(
        "login",
        reader = { app: Application -> Login(app.userData.username, app.userData.password) },
        endPoint = Login::class,
        writer = { loggedIn: LoggedIn ->
            { app: Application ->
                app.userData {
                    accessToken { loggedIn.accessToken }.
                    refreshToken { loggedIn.refreshToken }
                }
            }
        }
    )
}