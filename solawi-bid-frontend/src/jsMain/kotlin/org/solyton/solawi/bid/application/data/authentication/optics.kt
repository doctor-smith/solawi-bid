package org.solyton.solawi.bid.application.data.authentication

import org.evoleq.language.Lang
import org.evoleq.optics.lens.Lens
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.authentication.data.LoginForm
import org.solyton.solawi.bid.module.user.data.password
import org.solyton.solawi.bid.module.user.data.username
import org.solyton.solawi.bid.module.authentication.data.User as LoginUser

/*
val LoginForm : Lens<Application, LoginForm> by lazy {
    (userData pX i18N) * Lens<Pair<User, I18N>, LoginForm>(
        get = {pair -> LoginForm(
            LoginUser(
                pair.first.username,
                pair.first.password
            ),
            (pair.second.language ) as Lang.Block,

        )},
        set = {loginForm -> {pair ->
            pair.first{ User(
                loginForm.user.username,
                loginForm.user.password,
                "",
                ""
            ) }
        } }
    )
}

 */

val LoginForm : Lens<Application, LoginForm> by lazy {
    Lens(
        get = {whole -> LoginForm(
            LoginUser(
                whole.userData.username,
                whole.userData.password
            ),
            whole.i18N.language as Lang.Block,
            whole.deviceData.mediaType
        )},
        set = {
            loginForm -> {whole -> whole.userData {
                username { loginForm.user.username }
                .password{loginForm.user.password}
            }}
        }
    )
}
