package org.solyton.solawi.bid.application.ui.page.login.action

import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.localstorage.api.write
import org.solyton.solawi.bid.module.user.data.accessToken
import org.solyton.solawi.bid.module.user.data.password
import org.solyton.solawi.bid.module.user.data.refreshToken
import org.solyton.solawi.bid.module.user.data.username

@Markup
val logoutAction: Action<Application, Logout, Unit> by lazy {
    Action<Application, Logout, Unit>(
        name = "Logout",
        reader = {app: Application -> Logout(app.userData.refreshToken)},
        endPoint = Logout::class,
        writer = {_:Unit-> {app:Application ->
            write("accessToken", "")
            write("refreshToken", "")
            app.userData {
                refreshToken { "" }.
                accessToken { "" }.
                username { "" }.
                password { "" }
        }}}
    )
}