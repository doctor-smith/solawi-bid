package org.solyton.solawi.bid.module.navbar.action

import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.localstorage.api.write
import org.solyton.solawi.bid.module.navbar.data.navbar.NavBar
import org.solyton.solawi.bid.module.navbar.data.navbar.user
import org.solyton.solawi.bid.module.navbar.data.user.accessToken
import org.solyton.solawi.bid.module.navbar.data.user.password
import org.solyton.solawi.bid.module.navbar.data.user.refreshToken
import org.solyton.solawi.bid.module.navbar.data.user.username

@Markup
val logoutAction: Action<NavBar, Logout, Unit> by lazy {
    Action<NavBar, Logout, Unit>(
        name = "Logout",
        reader = {app: NavBar -> Logout(app.user.refreshToken)},
        endPoint = Logout::class,
        writer = {_:Unit-> {app:NavBar ->
            write("accessToken", "")
            write("refreshToken", "")
            app.user {
                refreshToken { "" }.
                accessToken { "" }.
                username { "" }.
                password { "" }
            }}}
    )
}
