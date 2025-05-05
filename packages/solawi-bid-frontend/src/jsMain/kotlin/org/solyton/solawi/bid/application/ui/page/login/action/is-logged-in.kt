package org.solyton.solawi.bid.application.ui.page.login.action

import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.authentication.data.api.IsLoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.LoggedInAs
import org.solyton.solawi.bid.module.localstorage.api.write
import org.solyton.solawi.bid.module.user.data.accessToken
import org.solyton.solawi.bid.module.user.data.refreshToken
import org.solyton.solawi.bid.module.user.data.username


@Markup
val isLoggedInAction: Action<Application, IsLoggedIn, LoggedInAs> by lazy {
    Action(
        "login",
        reader = { app: Application -> IsLoggedIn(app.userData.refreshToken) },
        endPoint = IsLoggedIn::class,
        writer = { loggedInAs: LoggedInAs -> {
            app -> if(loggedInAs.username == "" ){
                write("accessToken", "")
                write("refreshToken", "")
                app.userData {
                    accessToken { "" }.
                    refreshToken { "" }
                }
            } else {
                app.userData{
                    username { loggedInAs.username }.
                    refreshToken { loggedInAs.refreshToken }.
                    accessToken { loggedInAs.accessToken }
                }
            }
        }}
    )
}